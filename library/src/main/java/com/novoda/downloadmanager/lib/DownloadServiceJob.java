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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.PowerManager;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;

import com.novoda.downloadmanager.lib.logger.LLog;
import com.novoda.downloadmanager.notifications.DownloadNotifier;
import com.novoda.downloadmanager.notifications.DownloadNotifierFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Performs background downloads as requested by applications that use
 * {DownloadManager}. Multiple start commands can be issued at this
 * service, and it will continue running until no downloads are being actively
 * processed. It may schedule alarms to resume downloads in future.
 * <p/>
 * Any database updates important enough to initiate tasks should always be
 * delivered through {Context#startService(Intent)}.
 */
public class DownloadServiceJob {
    // TODO: migrate WakeLock from individual DownloadThreads out into
    // DownloadReceiver to protect our entire workflow.

    private final ContentLengthFetcher contentLengthFetcher = new ContentLengthFetcher();

    private SystemFacade systemFacade;
    private StorageManager storageManager;
    private DownloadScanner downloadScanner;
    private BatchRepository batchRepository;
    private DownloadsRepository downloadsRepository;
    private DownloadDeleter downloadDeleter;
    private DownloadReadyChecker downloadReadyChecker;
    private DownloadsUriProvider downloadsUriProvider;
    private BatchInformationBroadcaster batchInformationBroadcaster;
    private NetworkChecker networkChecker;
    private DestroyListener destroyListener;
    private NotificationsUpdator notificationsUpdator;

    @MainThread
    public static DownloadServiceJob getInstance() {
        return LazySingleton.INSTANCE;
    }

    private static class LazySingleton {
        private static final DownloadServiceJob INSTANCE = new DownloadServiceJob();
    }

    private DownloadServiceJob() {
        onCreate();
    }

    private void onCreate() {
        LLog.v("Ferran, Service onCreate");

        Context context = GlobalState.getContext();

        if (systemFacade == null) {
            systemFacade = new RealSystemFacade(context, new Clock());
        }

        this.downloadsUriProvider = DownloadsUriProvider.getInstance();
        this.downloadDeleter = new DownloadDeleter(context.getContentResolver());
        this.batchRepository = BatchRepository.from(context.getContentResolver(), downloadDeleter, downloadsUriProvider, systemFacade);
        this.networkChecker = new NetworkChecker(this.systemFacade);
        DownloadManagerModules modules = getDownloadManagerModules();
        this.destroyListener = modules.getDestroyListener();
        DownloadClientReadyChecker downloadClientReadyChecker = modules.getDownloadClientReadyChecker();
        PublicFacingDownloadMarshaller downloadMarshaller = new PublicFacingDownloadMarshaller();
        this.downloadReadyChecker = new DownloadReadyChecker(this.systemFacade, networkChecker, downloadClientReadyChecker, downloadMarshaller);

        String applicationPackageName = context.getApplicationContext().getPackageName();
        this.batchInformationBroadcaster = new BatchInformationBroadcaster(context, applicationPackageName);

        ContentResolver contentResolver = context.getContentResolver();
        File downloadDataDir = StorageManager.getDownloadDataDirectory(context);
        File externalStorageDir = Environment.getExternalStorageDirectory();
        File[] externalStorageDirs = new File[0];
        if (android.os.Build.VERSION.SDK_INT >= 19) {
            externalStorageDirs = context.getExternalFilesDirs(null);
        }
        File internalStorageDir = Environment.getDataDirectory();
        File systemCacheDir = Environment.getDownloadCacheDirectory();
        storageManager = new StorageManager(contentResolver, externalStorageDir, externalStorageDirs, internalStorageDir, systemCacheDir, downloadDataDir, downloadsUriProvider);

        downloadScanner = new DownloadScanner(context.getContentResolver(), context, downloadsUriProvider);

        DownloadNotifierFactory downloadNotifierFactory = new DownloadNotifierFactory();
        PublicFacingStatusTranslator statusTranslator = new PublicFacingStatusTranslator();

        this.downloadsRepository = new DownloadsRepository(
                systemFacade,
                context.getContentResolver(),
                new DownloadsRepository.DownloadInfoCreator() {
                    @Override
                    public FileDownloadInfo create(FileDownloadInfo.Reader reader) {
                        return createNewDownloadInfo(reader);
                    }
                },
                downloadsUriProvider
        );

        DownloadNotifier downloadNotifier = downloadNotifierFactory.getDownloadNotifier(context, modules, downloadMarshaller, statusTranslator);
        downloadNotifier.cancelAll();
        this.notificationsUpdator = new NotificationsUpdator(downloadNotifier, downloadsRepository, batchRepository);

        unlockStaleDownloads();
    }

    private void unlockStaleDownloads() {
        List<String> batchesToBeUnlocked = downloadsRepository.getCurrentDownloadingOrSubmittedBatchIds();
        if (batchesToBeUnlocked.isEmpty()) {
            return;
        }

        downloadsRepository.updateRunningOrSubmittedDownloadsToPending();
        batchRepository.updateBatchesToPendingStatus(batchesToBeUnlocked);
    }

    /**
     * Keeps a local copy of the info about a download, and initiates the
     * download if appropriate.
     */
    private FileDownloadInfo createNewDownloadInfo(FileDownloadInfo.Reader reader) {
        FileDownloadInfo info = reader.newDownloadInfo(systemFacade, downloadsUriProvider);
        LLog.v("processing inserted download " + info.getId());
        return info;
    }

    private DownloadManagerModules getDownloadManagerModules() {
        Context applicationContext = GlobalState.getContext().getApplicationContext();

        if (applicationContext instanceof DownloadManagerModules.Provider) {
            return ((DownloadManagerModules.Provider) applicationContext).provideDownloadManagerModules();
        } else {
            return new DefaultsDownloadManagerModules(applicationContext);
        }
    }

    @WorkerThread
    public int onStartCommand(boolean enforceExecution) {
        LLog.v("Ferran, Service onStartCommand");
        return startDownloading(enforceExecution);
    }

    private int startDownloading(boolean enforceExecution) {
        LLog.v("Ferran, startDownloading");

        if (!enforceExecution && alreadyDownloading()) {
            LLog.v("Ferran, batch already running, we won't continue");
            return DownloadStatus.SUCCESS;
        }

        PowerManager.WakeLock wakeLock = getWakeLock();

        wakeLock.acquire();
        downloadUntilAllBatchesAreInactive();
        wakeLock.release();

        shutDown();

        return getDownloadStatus();
    }

    private void downloadUntilAllBatchesAreInactive() {
        boolean isActive = download();

        while (isActive) {
            waitForOneSecond();
            isActive = download();
        }
    }

    private void waitForOneSecond() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int getDownloadStatus() {
        List<DownloadBatch> downloadBatches = getDownloadBatches();

        if (downloadBatches.isEmpty()) {
            return DownloadStatus.SUCCESS;
        }

        int statusInPriority = 0;

        for (DownloadBatch downloadBatch : downloadBatches) {
            int status = downloadBatch.getStatus();
            if (DownloadStatus.isPendingForNetwork(status)) {
                return DownloadStatus.WAITING_FOR_NETWORK;
            } else {
                statusInPriority = status;
            }
        }

        return statusInPriority;
    }

    private boolean alreadyDownloading() {
        List<DownloadBatch> downloadBatches = getDownloadBatches();

        for (DownloadBatch downloadBatch : downloadBatches) {
            if (downloadBatch.isRunning()) {
                return true;
            }
        }

        return false;
    }

    private List<DownloadBatch> getDownloadBatches() {
        Collection<FileDownloadInfo> allDownloads = downloadsRepository.getAllDownloads();
        return batchRepository.retrieveBatchesFor(allDownloads);
    }

    private PowerManager.WakeLock getWakeLock() {
        PowerManager pm = (PowerManager) GlobalState.getContext().getSystemService(Context.POWER_SERVICE);
        return pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Long running task");
    }

    private void shutDown() {
        LLog.d("Ferran, Shutting down service");
        destroyListener.onDownloadManagerModulesDestroyed();
        downloadScanner.shutdown();
    }

    /**
     * Update {#downloads} to match {DownloadProvider} state.
     * Depending on current download state it may enqueue {DownloadTask}
     * instances, request {DownloadScanner} scans, update user-visible
     * notifications, and/or schedule future actions with {AlarmManager}.
     * <p/>
     * Should only be called from {#updateThread} as after being
     * requested through {#enqueueUpdate()}.
     * for (DownloadInfo info : downloadBatch.getDownloads()) {
     * if (info.isDeleted) {
     * snapshot taken in this update.
     */
    private boolean download() {
        LLog.v("Ferran, start updateLocked");
        boolean isActive = false;

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
                boolean isBatchStartingForTheFirstTime = batchRepository.isBatchStartingForTheFirstTime(downloadBatch.getBatchId());
                if (isBatchStartingForTheFirstTime) {
                    handleBatchStartingForTheFirstTime(downloadBatch);
                }

                downloadOrContinueBatch(downloadBatch.getDownloads());
                isActive = true;
            } else if (downloadBatch.scanCompletedMediaIfReady(downloadScanner)) {
                isActive = true;
            }
        }

        batchRepository.deleteMarkedBatchesFor(allDownloads);
        notificationsUpdator.updateImmediately(downloadBatches);

        if (!isActive) {
            moveSubmittedTasksToBatchStatusIfNecessary();
        }

        LLog.v("Ferran, end updateLocked isActive: " + isActive);

        return isActive;
    }

    private void handleBatchStartingForTheFirstTime(DownloadBatch downloadBatch) {
        batchRepository.markBatchAsStarted(downloadBatch.getBatchId());
        batchInformationBroadcaster.notifyBatchStartedFor(downloadBatch.getBatchId());
    }

    private void moveSubmittedTasksToBatchStatusIfNecessary() {
        List<DownloadBatch> downloadBatches = getDownloadBatches();

        for (DownloadBatch downloadBatch : downloadBatches) {
            List<Long> ids = getSubmittedDownloadIdsFrom(downloadBatch);
            downloadsRepository.moveDownloadsStatusTo(ids, downloadBatch.getStatus());
        }
    }

    private List<Long> getSubmittedDownloadIdsFrom(DownloadBatch downloadBatch) {
        List<Long> ids = new ArrayList<>();
        List<FileDownloadInfo> downloads = downloadBatch.getDownloads();
        for (FileDownloadInfo downloadInfo : downloads) {
            if (downloadInfo.getStatus() == DownloadStatus.SUBMITTED) {
                ids.add(downloadInfo.getId());
            }
        }
        return ids;
    }

    private void downloadOrContinueBatch(List<FileDownloadInfo> downloads) {
        for (FileDownloadInfo info : downloads) {
            if (!DownloadStatus.isCompleted(info.getStatus()) && !info.isSubmittedOrRunning()) {
                download(info);
                return;
            }
        }
    }

    private void download(FileDownloadInfo info) {
        LLog.v("Ferran, Download " + info.getId());
        Context context = GlobalState.getContext();
        Uri downloadUri = ContentUris.withAppendedId(downloadsUriProvider.getAllDownloadsUri(), info.getId());
        FileDownloadInfo.ControlStatus.Reader controlReader = new FileDownloadInfo.ControlStatus.Reader(context.getContentResolver(), downloadUri);
        DownloadBatch downloadBatch = batchRepository.retrieveBatchFor(info);
        DownloadTask downloadTask = new DownloadTask(
                context, systemFacade, info, downloadBatch, storageManager, notificationsUpdator,
                batchInformationBroadcaster, batchRepository,
                controlReader, networkChecker, downloadReadyChecker, new Clock(),
                downloadsRepository
        );

        downloadsRepository.setDownloadSubmitted(info);

        int batchStatus = batchRepository.calculateBatchStatus(info.getBatchId());
        batchRepository.updateBatchStatus(info.getBatchId(), batchStatus);

        notificationsUpdator.startUpdating();
        downloadTask.run();
        notificationsUpdator.stopUpdating();
    }

    private void updateTotalBytesFor(Collection<FileDownloadInfo> downloadInfos) {
        Context context = GlobalState.getContext();
        ContentValues values = new ContentValues(1);
        for (FileDownloadInfo downloadInfo : downloadInfos) {
            if (downloadInfo.hasUnknownTotalBytes()) {
                long totalBytes = contentLengthFetcher.fetchContentLengthFor(downloadInfo);
                values.put(DownloadContract.Downloads.COLUMN_TOTAL_BYTES, totalBytes);
                context.getContentResolver().update(downloadInfo.getAllDownloadsUri(), values, null, null);
            }
        }
    }
}
