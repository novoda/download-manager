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
    private BatchRepository batchRepository;
    private DownloadsRepository downloadsRepository;
    private DownloadDeleter downloadDeleter;
    private DownloadReadyChecker downloadReadyChecker;
    private DownloadsUriProvider downloadsUriProvider;
    private BatchCompletionBroadcaster batchCompletionBroadcaster;
    private NetworkChecker networkChecker;

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

        if (systemFacade == null) {
            systemFacade = new RealSystemFacade(this);
        }

        this.downloadsUriProvider = DownloadsUriProvider.getInstance();
        this.downloadDeleter = new DownloadDeleter(getContentResolver());
        this.batchRepository = new BatchRepository(getContentResolver(), downloadDeleter, downloadsUriProvider, systemFacade);
        PublicFacingDownloadMarshaller downloadMarshaller = new PublicFacingDownloadMarshaller();
        DownloadClientReadyChecker downloadClientReadyChecker = getDownloadClientReadyChecker();
        this.networkChecker = new NetworkChecker(this.systemFacade);
        this.downloadReadyChecker = new DownloadReadyChecker(this.systemFacade, networkChecker, downloadClientReadyChecker, downloadMarshaller);

        String applicationPackageName = getApplicationContext().getPackageName();
        this.batchCompletionBroadcaster = new BatchCompletionBroadcaster(this, applicationPackageName);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        ContentResolver contentResolver = getContentResolver();
        File downloadDataDir = StorageManager.getDownloadDataDirectory(this);
        File externalStorageDir = Environment.getExternalStorageDirectory();
        File internalStorageDir = Environment.getDataDirectory();
        File systemCacheDir = Environment.getDownloadCacheDirectory();
        storageManager = new StorageManager(contentResolver, externalStorageDir, internalStorageDir, systemCacheDir, downloadDataDir, downloadsUriProvider);

        updateThread = new HandlerThread("DownloadManager-UpdateThread");
        updateThread.start();
        updateHandler = new Handler(updateThread.getLooper(), updateCallback);

        downloadScanner = new DownloadScanner(getContentResolver(), this, downloadsUriProvider);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationDisplayer notificationDisplayer = new NotificationDisplayer(
                this,
                notificationManager,
                getNotificationImageRetriever(),
                getResources(),
                downloadsUriProvider);

        downloadNotifier = new DownloadNotifier(this, notificationDisplayer);
        downloadNotifier.cancelAll();

        downloadManagerContentObserver = new DownloadManagerContentObserver();
        getContentResolver().registerContentObserver(
                downloadsUriProvider.getAllDownloadsUri(),
                true, downloadManagerContentObserver);

        PackageManager packageManager = getPackageManager();
        String packageName = getApplicationContext().getPackageName();
        ConcurrentDownloadsLimitProvider concurrentDownloadsLimitProvider = new ConcurrentDownloadsLimitProvider(packageManager, packageName);
        DownloadExecutorFactory factory = new DownloadExecutorFactory(concurrentDownloadsLimitProvider);
        executor = factory.createExecutor();

        this.downloadsRepository = new DownloadsRepository(
                getContentResolver(), new DownloadsRepository.DownloadInfoCreator() {
            @Override
            public FileDownloadInfo create(FileDownloadInfo.Reader reader) {
                return createNewDownloadInfo(reader);
            }

            @Override
            public FileDownloadInfo.ControlStatus create(FileDownloadInfo.ControlStatus.Reader reader, long id) {
                return createNewDownloadInfoControlStatus(reader, id);
            }
        }, downloadsUriProvider);

    }

    /**
     * Keeps a local copy of the info about a download, and initiates the
     * download if appropriate.
     */
    private FileDownloadInfo createNewDownloadInfo(FileDownloadInfo.Reader reader) {
        FileDownloadInfo info = reader.newDownloadInfo(systemFacade, downloadsUriProvider);
        Log.v("processing inserted download " + info.getId());
        return info;
    }

    private FileDownloadInfo.ControlStatus createNewDownloadInfoControlStatus(FileDownloadInfo.ControlStatus.Reader reader, long id) {
        return reader.newControlStatus(id);
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
            if (downloadBatch.isActive()) {
                isActive = true;
                break;
            }
        }

        for (DownloadBatch downloadBatch : downloadBatches) {
            if (downloadBatch.isDeleted() || downloadBatch.prune(downloadDeleter)) {
                continue;
            }

            if (!isActive && downloadReadyChecker.canDownload(downloadBatch)) {
                downloadOrContinueBatch(downloadBatch.getDownloads());
                isActive = true;
            } else if (downloadBatch.scanCompletedMediaIfReady(downloadScanner)) {
                isActive = true;
            }

            nextRetryTimeMillis = downloadBatch.nextActionMillis(now, nextRetryTimeMillis);
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

    private void downloadOrContinueBatch(List<FileDownloadInfo> downloads) {
        for (FileDownloadInfo info : downloads) {
            if (!DownloadStatus.isCompleted(info.getStatus()) && !info.isSubmittedOrRunning()) {
                download(info);
            }
        }
    }

    private void download(FileDownloadInfo info) {
        DownloadThread downloadThread = new DownloadThread(
                this, systemFacade, info, storageManager, downloadNotifier,
                batchCompletionBroadcaster, batchRepository, downloadsUriProvider, downloadsRepository, networkChecker, downloadReadyChecker);

        ContentValues contentValues = new ContentValues();
        contentValues.put(DownloadContract.Downloads.COLUMN_STATUS, DownloadStatus.SUBMITTED);
        getContentResolver().update(info.getAllDownloadsUri(), contentValues, null, null);

        executor.submit(downloadThread);
    }

    private void updateTotalBytesFor(Collection<FileDownloadInfo> downloadInfos) {
        ContentValues values = new ContentValues(1);
        for (FileDownloadInfo downloadInfo : downloadInfos) {
            if (downloadInfo.hasUnknownTotalBytes()) {
                long totalBytes = contentLengthFetcher.fetchContentLengthFor(downloadInfo);
                values.put(DownloadContract.Downloads.COLUMN_TOTAL_BYTES, totalBytes);
                getContentResolver().update(downloadInfo.getAllDownloadsUri(), values, null, null);
            }
        }
    }

    private void updateUserVisibleNotification(Collection<DownloadBatch> batches) {
        downloadNotifier.updateWith(batches);
    }

    @Override
    protected void dump(FileDescriptor fd, @NonNull PrintWriter writer, String[] args) {
        Log.e("I want to dump but nothing to dump into");
    }
}
