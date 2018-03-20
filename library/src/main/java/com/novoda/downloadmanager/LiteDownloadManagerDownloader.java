package com.novoda.downloadmanager;

import android.os.Handler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DELETED;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DELETING;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DOWNLOADED;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.PAUSED;

class LiteDownloadManagerDownloader {

    private final Object waitForDownloadService;
    private final Object waitForDownloadBatchStatusCallback;
    private final ExecutorService executor;
    private final Handler callbackHandler;
    private final FileOperations fileOperations;
    private final DownloadsBatchPersistence downloadsBatchPersistence;
    private final DownloadsFilePersistence downloadsFilePersistence;
    private final DownloadBatchStatusNotificationDispatcher notificationDispatcher;
    private final List<DownloadBatchStatusCallback> callbacks;
    private final ConnectionChecker connectionChecker;

    private final CallbackThrottleCreator callbackThrottleCreator;

    private DownloadService downloadService;

    @SuppressWarnings({"checkstyle:parameternumber", "PMD.ExcessiveParameterList"})
// Can't group anymore these are customisable options.
    LiteDownloadManagerDownloader(Object waitForDownloadService,
                                  Object waitForDownloadBatchStatusCallback,
                                  ExecutorService executor,
                                  Handler callbackHandler,
                                  FileOperations fileOperations,
                                  DownloadsBatchPersistence downloadsBatchPersistence,
                                  DownloadsFilePersistence downloadsFilePersistence,
                                  DownloadBatchStatusNotificationDispatcher notificationDispatcher,
                                  ConnectionChecker connectionChecker,
                                  List<DownloadBatchStatusCallback> callbacks,
                                  CallbackThrottleCreator callbackThrottleCreator) {
        this.waitForDownloadService = waitForDownloadService;
        this.waitForDownloadBatchStatusCallback = waitForDownloadBatchStatusCallback;
        this.executor = executor;
        this.callbackHandler = callbackHandler;
        this.fileOperations = fileOperations;
        this.downloadsBatchPersistence = downloadsBatchPersistence;
        this.downloadsFilePersistence = downloadsFilePersistence;
        this.notificationDispatcher = notificationDispatcher;
        this.connectionChecker = connectionChecker;
        this.callbacks = callbacks;
        this.callbackThrottleCreator = callbackThrottleCreator;
    }

    public void download(Batch batch, Map<DownloadBatchId, DownloadBatch> downloadBatchMap) {
        DownloadBatch downloadBatch = DownloadBatchFactory.newInstance(
                batch,
                fileOperations,
                downloadsBatchPersistence,
                downloadsFilePersistence,
                callbackThrottleCreator.create(),
                connectionChecker
        );

        downloadBatchMap.put(downloadBatch.getId(), downloadBatch);
        download(downloadBatch, downloadBatchMap);
    }

    public void download(DownloadBatch downloadBatch, Map<DownloadBatchId, DownloadBatch> downloadBatchMap) {
        DownloadBatchId downloadBatchId = downloadBatch.getId();
        if (!downloadBatchMap.containsKey(downloadBatchId)) {
            downloadBatchMap.put(downloadBatchId, downloadBatch);
        }

        executor.submit(() -> Wait.<Void>waitFor(downloadService, waitForDownloadService)
                .thenPerform(executeDownload(downloadBatch, downloadBatchMap)));
    }

    private Wait.ThenPerform.Action<Void> executeDownload(DownloadBatch downloadBatch, Map<DownloadBatchId, DownloadBatch> downloadBatchMap) {
        return () -> {
            downloadBatch.persistAsync();
            InternalDownloadBatchStatus downloadBatchStatus = downloadBatch.status();
            updateStatusToQueuedIfNeeded(downloadBatchStatus);
            downloadService.download(downloadBatch, downloadBatchCallback(downloadBatchMap));
            return null;
        };
    }

    private void updateStatusToQueuedIfNeeded(InternalDownloadBatchStatus downloadBatchStatus) {
        DownloadBatchStatus.Status status = downloadBatchStatus.status();
        if (status != PAUSED && status != DOWNLOADED && status != DELETING && status != DELETED) {
            downloadBatchStatus.markAsQueued(downloadsBatchPersistence);
        }
    }

    private DownloadBatchStatusCallback downloadBatchCallback(Map<DownloadBatchId, DownloadBatch> downloadBatchMap) {
        return downloadBatchStatus -> {
            if (downloadBatchStatus == null) {
                return;
            }

            DownloadBatchId downloadBatchId = downloadBatchStatus.getDownloadBatchId();
            if (downloadBatchStatus.status() == DELETED) {
                Logger.v("batch " + downloadBatchId.rawId() + " is finally deleted, removing it from the map");
                downloadBatchMap.remove(downloadBatchId);
            }

            callbackHandler.post(() -> {
                synchronized (waitForDownloadBatchStatusCallback) {
                    for (DownloadBatchStatusCallback callback : callbacks) {
                        callback.onUpdate(downloadBatchStatus);
                    }
                    notificationDispatcher.updateNotification(downloadBatchStatus);
                }
            });
        };
    }

    void setDownloadService(DownloadService downloadService) {
        this.downloadService = downloadService;
        notificationDispatcher.setDownloadService(downloadService);
    }
}
