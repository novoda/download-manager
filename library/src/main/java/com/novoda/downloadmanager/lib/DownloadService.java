/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.novoda.downloadmanager.lib;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;

import com.novoda.notils.logger.simple.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

/**
 * Performs background downloads as requested by applications that use
 * {DownloadManager}. Multiple start commands can be issued at this
 * service, and it will continue running until no downloads are being actively
 * processed. It may schedule alarms to resume downloads in future.
 * <p/>
 * Any database updates important enough to initiate tasks should always be
 * delivered through {Context#startService(Intent)}.
 */
public class DownloadService extends Service {
    // TODO: migrate WakeLock from individual DownloadThreads out into
    // DownloadReceiver to protect our entire workflow.

    private static final boolean DEBUG_LIFECYCLE = false;
    private final ContentLengthFetcher contentLengthFetcher = new ContentLengthFetcher();

    private SystemFacade systemFacade;
    private AlarmManager alarmManager;
    private StorageManager storageManager;
    private DownloadManagerContentObserver downloadManagerContentObserver;
    private DownloadNotifier downloadNotifier;
    private ExecutorService executor;
    private DownloadScanner downloadScanner;

    private HandlerThread updateThread;
    private Handler updateHandler;

    private volatile int lastStartId;
    private DownloadClientReadyChecker downloadClientReadyChecker;
    private BatchRepository batchRepository;
    private DownloadsRepository downloadsRepository;
    private DownloadDeleter downloadDeleter;

    /**
     * Receives notifications when the data in the content provider changes
     */
    private class DownloadManagerContentObserver extends ContentObserver {
        public DownloadManagerContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(final boolean selfChange) {
            enqueueUpdate();
        }
    }

    /**
     * Returns an IBinder instance when someone wants to connect to this
     * service. Binding to this service is not allowed.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public IBinder onBind(@NonNull Intent intent) {
        throw new UnsupportedOperationException("Cannot bind to Download Manager Service");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("Service onCreate");

        this.downloadDeleter = new DownloadDeleter(getContentResolver());
        this.batchRepository = BatchRepository.newInstance(getContentResolver(), downloadDeleter);

        if (systemFacade == null) {
            systemFacade = new RealSystemFacade(this);
        }

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        ContentResolver contentResolver = getContentResolver();
        File downloadDataDir = StorageManager.getDownloadDataDirectory(this);
        File externalStorageDir = Environment.getExternalStorageDirectory();
        File internalStorageDir = Environment.getDataDirectory();
        File systemCacheDir = Environment.getDownloadCacheDirectory();
        storageManager = new StorageManager(contentResolver, externalStorageDir, internalStorageDir, systemCacheDir, downloadDataDir);

        updateThread = new HandlerThread("DownloadManager-UpdateThread");
        updateThread.start();
        updateHandler = new Handler(updateThread.getLooper(), updateCallback);

        downloadScanner = new DownloadScanner(getContentResolver(), this);

        downloadClientReadyChecker = getDownloadClientReadyChecker();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationDisplayer notificationDisplayer = new NotificationDisplayer(
                this,
                notificationManager,
                getNotificationImageRetriever(),
                getResources());

        downloadNotifier = new DownloadNotifier(this, notificationDisplayer);
        downloadNotifier.cancelAll();

        downloadManagerContentObserver = new DownloadManagerContentObserver();
        getContentResolver().registerContentObserver(
                Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI,
                true, downloadManagerContentObserver);

        PackageManager packageManager = getPackageManager();
        String packageName = getApplicationContext().getPackageName();
        ConcurrentDownloadsLimitProvider concurrentDownloadsLimitProvider = new ConcurrentDownloadsLimitProvider(packageManager, packageName);
        DownloadExecutorFactory factory = new DownloadExecutorFactory(concurrentDownloadsLimitProvider);
        executor = factory.createExecutor();

        this.downloadsRepository = new DownloadsRepository(getContentResolver(), new DownloadsRepository.DownloadInfoCreator() {
            @Override
            public FileDownloadInfo create(FileDownloadInfo.Reader reader) {
                return createNewDownloadInfo(reader);
            }
        });

    }

    /**
     * Keeps a local copy of the info about a download, and initiates the
     * download if appropriate.
     */
    private FileDownloadInfo createNewDownloadInfo(FileDownloadInfo.Reader reader) {
        FileDownloadInfo info = reader.newDownloadInfo(this, systemFacade, downloadClientReadyChecker);
        Log.v("processing inserted download " + info.getId());
        return info;
    }

    private DownloadClientReadyChecker getDownloadClientReadyChecker() {
        if (getApplication() instanceof DownloadClientReadyChecker) {
            return (DownloadClientReadyChecker) getApplication();
        }
        return DownloadClientReadyChecker.READY;
    }

    private NotificationImageRetriever getNotificationImageRetriever() {
        if (getApplication() instanceof NotificationImageRetrieverFactory) {
            return ((NotificationImageRetrieverFactory) getApplication()).createNotificationImageRetriever();
        }
        return new OkHttpNotificationImageRetriever();
    }

    @Override
    public int onStartCommand(@NonNull Intent intent, int flags, int startId) {
        int returnValue = super.onStartCommand(intent, flags, startId);
        Log.v("Service onStart");
        lastStartId = startId;
        enqueueUpdate();
        return returnValue;
    }

    @Override
    public void onDestroy() {
        shutDown();
        Log.v("Service onDestroy");
        super.onDestroy();
    }

    private void shutDown() {
        Log.d("Shutting down service");
        getContentResolver().unregisterContentObserver(downloadManagerContentObserver);
        downloadScanner.shutdown();
        updateThread.quit();
    }

    /**
     * Enqueue an {#updateLocked()} pass to occur in future.
     */
    private void enqueueUpdate() {
        updateHandler.removeMessages(MSG_UPDATE);
        updateHandler.obtainMessage(MSG_UPDATE, lastStartId, -1).sendToTarget();
    }

    /**
     * Enqueue an {#updateLocked()} pass to occur after delay, usually to
     * catch any finished operations that didn't trigger an update pass.
     */
    private void enqueueFinalUpdate() {
        updateHandler.removeMessages(MSG_FINAL_UPDATE);
        updateHandler.sendMessageDelayed(
                updateHandler.obtainMessage(MSG_FINAL_UPDATE, lastStartId, -1),
                5 * MINUTE_IN_MILLIS);
    }

    private static final int MSG_UPDATE = 1;
    private static final int MSG_FINAL_UPDATE = 2;

    private final Handler.Callback updateCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            final int startId = msg.arg1;
            if (DEBUG_LIFECYCLE) {
                Log.v("Updating for startId " + startId);
            }

            // Since database is current source of truth, our "active" status
            // depends on database state. We always get one final update pass
            // once the real actions have finished and persisted their state.

            // TODO: switch to asking real tasks to derive active state
            // TODO: handle media scanner timeouts

            boolean isActive = updateLocked();

            if (msg.what == MSG_FINAL_UPDATE) {
                // Dump thread stacks belonging to pool
                for (Map.Entry<Thread, StackTraceElement[]> entry :
                        Thread.getAllStackTraces().entrySet()) {
                    if (entry.getKey().getName().startsWith("pool")) {
                        Log.d(entry.getKey() + ": " + Arrays.toString(entry.getValue()));
                    }
                }

                // Dump speed and update details
                downloadNotifier.dumpSpeeds();

                Log.wtf("Final update pass triggered, isActive=" + isActive, new IllegalStateException("someone didn't update correctly"));
            }

            if (isActive) {
                // Still doing useful work, keep service alive. These active
                // tasks will trigger another update pass when they're finished.

                // Enqueue delayed update pass to catch finished operations that
                // didn't trigger an update pass; these are bugs.
                enqueueFinalUpdate();

            } else {
                // No active tasks, and any pending update messages can be
                // ignored, since any updates important enough to initiate tasks
                // will always be delivered with a new startId.

                if (stopSelfResult(startId)) {
                    if (DEBUG_LIFECYCLE) {
                        Log.v("Nothing left; stopped");
                    }
                    shutDown();
                }
            }

            return true;
        }
    };

    /**
     * Update {#downloads} to match {DownloadProvider} state.
     * Depending on current download state it may enqueue {DownloadThread}
     * instances, request {DownloadScanner} scans, update user-visible
     * notifications, and/or schedule future actions with {AlarmManager}.
     * <p/>
     * Should only be called from {#updateThread} as after being
     * requested through {#enqueueUpdate()}.
     * for (DownloadInfo info : downloadBatch.getDownloads()) {
     * if (info.isDeleted) {
     * snapshot taken in this update.
     */
    private boolean updateLocked() {

        boolean isActive = false;
        long nextRetryTimeMillis = Long.MAX_VALUE;
        long now = systemFacade.currentTimeMillis();

        Collection<FileDownloadInfo> allDownloads = downloadsRepository.getAllDownloads();
        updateTotalBytesFor(allDownloads);

        List<DownloadBatch> downloadBatches = batchRepository.retrieveBatchesFor(allDownloads);
        for (DownloadBatch downloadBatch : downloadBatches) {

            for (FileDownloadInfo info : downloadBatch.getDownloads()) {
                if (info.isDeleted()) {
                    downloadDeleter.deleteFileAndDatabaseRow(info);
                } else if (Downloads.Impl.isStatusCancelled(info.getStatus()) || Downloads.Impl.isStatusError(info.getStatus())) {
                    downloadDeleter.deleteFileAndMediaReference(info);
                } else {
                    if (downloadBatch.isDeleted()) {
                        break;
                    }

                    isActive = kickOffDownloadTaskIfReady(isActive, info, downloadBatch);
                    isActive = kickOffMediaScanIfCompleted(isActive, info);
                }

                // Keep track of nearest next action
                nextRetryTimeMillis = Math.min(info.nextActionMillis(now), nextRetryTimeMillis);
            }

        }

        batchRepository.deleteMarkedBatchesFor(allDownloads);
        updateUserVisibleNotification(downloadBatches);

        // Set alarm when next action is in future. It's okay if the service
        // continues to run in meantime, since it will kick off an update pass.
        if (nextRetryTimeMillis > 0 && nextRetryTimeMillis < Long.MAX_VALUE) {
            Log.v("scheduling start in " + nextRetryTimeMillis + "ms");

            Intent intent = new Intent(Constants.ACTION_RETRY);
            intent.setClass(this, DownloadReceiver.class);
            alarmManager.set(AlarmManager.RTC_WAKEUP, now + nextRetryTimeMillis, PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT));
        }

        return isActive;
    }

    private void updateTotalBytesFor(Collection<FileDownloadInfo> downloadInfos) {
        ContentValues values = new ContentValues();
        for (FileDownloadInfo downloadInfo : downloadInfos) {
            if (downloadInfo.getTotalBytes() == -1) {
                long totalBytes = contentLengthFetcher.fetchContentLengthFor(downloadInfo);
                values.put(Downloads.Impl.COLUMN_TOTAL_BYTES, totalBytes);
                getContentResolver().update(downloadInfo.getAllDownloadsUri(), values, null, null);

                batchRepository.updateCurrentSize(downloadInfo.getBatchId());
                batchRepository.updateTotalSize(downloadInfo.getBatchId());
            }
        }
    }

    private boolean kickOffDownloadTaskIfReady(boolean isActive, FileDownloadInfo info, DownloadBatch downloadBatch) {
        boolean downloadIsActive = info.isActive();

        if (!downloadIsActive || info.isReadyToDownload(downloadBatch)) {
            isActive |= info.startDownloadIfNotActive(executor, storageManager, downloadNotifier);
        }
        return isActive;
    }

    private boolean kickOffMediaScanIfCompleted(boolean isActive, FileDownloadInfo info) {
        final boolean activeScan = info.startScanIfReady(downloadScanner);
        isActive |= activeScan;
        return isActive;
    }

    private void updateUserVisibleNotification(Collection<DownloadBatch> batches) {
        downloadNotifier.updateWith(batches);
    }

    @Override
    protected void dump(FileDescriptor fd, @NonNull PrintWriter writer, String[] args) {
        Log.e("I want to dump but nothing to dump into");
    }
}
