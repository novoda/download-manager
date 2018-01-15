package com.novoda.downloadmanager;

import android.os.Handler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DOWNLOADED;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.PAUSED;

class LiteDownloadManagerDownloader {

    private final Object waitForDownloadService;
    private final ExecutorService executor;
    private final Handler callbackHandler;
    private final FileOperations fileOperations;
    private final DownloadsBatchPersistence downloadsBatchPersistence;
    private final DownloadsFilePersistence downloadsFilePersistence;
    private final NotificationDispatcher notificationDispatcher;
    private final List<DownloadBatchCallback> callbacks;
    private final CallbackThrottleCreator callbackThrottleCreator;

    private DownloadService downloadService;

    @SuppressWarnings({"checkstyle:parameternumber", "PMD.ExcessiveParameterList"})
// Can't group anymore these are customisable options.
    LiteDownloadManagerDownloader(Object waitForDownloadService,
                                  ExecutorService executor,
                                  Handler callbackHandler,
                                  FileOperations fileOperations,
                                  DownloadsBatchPersistence downloadsBatchPersistence,
                                  DownloadsFilePersistence downloadsFilePersistence,
                                  NotificationDispatcher notificationDispatcher,
                                  List<DownloadBatchCallback> callbacks,
                                  CallbackThrottleCreator callbackThrottleCreator) {
        this.waitForDownloadService = waitForDownloadService;
        this.executor = executor;
        this.callbackHandler = callbackHandler;
        this.fileOperations = fileOperations;
        this.downloadsBatchPersistence = downloadsBatchPersistence;
        this.downloadsFilePersistence = downloadsFilePersistence;
        this.notificationDispatcher = notificationDispatcher;
        this.callbacks = callbacks;
        this.callbackThrottleCreator = callbackThrottleCreator;
    }

    public void download(Batch batch, Map<DownloadBatchId, DownloadBatch> downloadBatchMap) {
        DownloadBatch runningDownloadBatch = downloadBatchMap.get(batch.getDownloadBatchId());
        if (runningDownloadBatch != null) {
            return;
        }

        CallbackThrottle callbackThrottle = callbackThrottleCreator.create();

        DownloadBatch downloadBatch = DownloadBatchFactory.newInstance(
                batch,
                fileOperations,
                downloadsBatchPersistence,
                downloadsFilePersistence,
                callbackThrottle
        );

        downloadBatch.persist();
        download(downloadBatch, downloadBatchMap);
    }

    public void download(DownloadBatch downloadBatch, Map<DownloadBatchId, DownloadBatch> downloadBatchMap) {
        downloadBatchMap.put(downloadBatch.getId(), downloadBatch);
        executor.submit(() -> WaitForDownloadService.<Void>waitFor(downloadService, waitForDownloadService)
                .thenPerform(executeDownload(downloadBatch)));
    }

    private WaitForDownloadService.ThenPerform.Action<Void> executeDownload(DownloadBatch downloadBatch) {
        return () -> {
            InternalDownloadBatchStatus downloadBatchStatus = downloadBatch.status();
            updateStatusToQueuedIfNeeded(downloadBatchStatus);
            downloadService.download(downloadBatch, downloadBatchCallback());
            return null;
        };
    }

    private void updateStatusToQueuedIfNeeded(InternalDownloadBatchStatus downloadBatchStatus) {
        if (downloadBatchStatus.status() != PAUSED && downloadBatchStatus.status() != DOWNLOADED) {
            downloadBatchStatus.markAsQueued(downloadsBatchPersistence);
        }
    }

    private DownloadBatchCallback downloadBatchCallback() {
        return downloadBatchStatus -> callbackHandler.post(() -> {
            for (DownloadBatchCallback callback : callbacks) {
                callback.onUpdate(downloadBatchStatus);
            }
            notificationDispatcher.updateNotification(downloadBatchStatus);
        });
    }

    void setDownloadService(DownloadService downloadService) {
        this.downloadService = downloadService;
        notificationDispatcher.setDownloadService(downloadService);
    }
}
