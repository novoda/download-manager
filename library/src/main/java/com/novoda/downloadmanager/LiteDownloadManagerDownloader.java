package com.novoda.downloadmanager;

import android.os.Handler;

import com.novoda.notils.logger.simple.Log;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DOWNLOADED;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.PAUSED;

class LiteDownloadManagerDownloader {

    private final Object waitForDownloadService;
    private final ExecutorService executor;
    private final Handler callbackHandler;
    private final FileOperations fileOperations;
    private final DownloadsBatchPersistence downloadsBatchPersistence;
    private final DownloadsFilePersistence downloadsFilePersistence;
    private final DownloadBatchStatusNotificationDispatcher notificationDispatcher;
    private final List<DownloadBatchStatusCallback> callbacks;
    private final ConnectionChecker connectionChecker;

    private final CallbackThrottleCreator callbackThrottleCreator;
    private final Semaphore semaphore;

    private DownloadService downloadService;

    @SuppressWarnings({"checkstyle:parameternumber", "PMD.ExcessiveParameterList"})
// Can't group anymore these are customisable options.
    LiteDownloadManagerDownloader(Object waitForDownloadService,
                                  ExecutorService executor,
                                  Handler callbackHandler,
                                  FileOperations fileOperations,
                                  DownloadsBatchPersistence downloadsBatchPersistence,
                                  DownloadsFilePersistence downloadsFilePersistence,
                                  DownloadBatchStatusNotificationDispatcher notificationDispatcher,
                                  ConnectionChecker connectionChecker,
                                  List<DownloadBatchStatusCallback> callbacks,
                                  CallbackThrottleCreator callbackThrottleCreator,
                                  Semaphore semaphore) {
        this.waitForDownloadService = waitForDownloadService;
        this.executor = executor;
        this.callbackHandler = callbackHandler;
        this.fileOperations = fileOperations;
        this.downloadsBatchPersistence = downloadsBatchPersistence;
        this.downloadsFilePersistence = downloadsFilePersistence;
        this.notificationDispatcher = notificationDispatcher;
        this.connectionChecker = connectionChecker;
        this.callbacks = callbacks;
        this.callbackThrottleCreator = callbackThrottleCreator;
        this.semaphore = semaphore;
    }

    public void download(Batch batch, Map<DownloadBatchId, DownloadBatch> downloadBatchMap) {
        DownloadBatch runningDownloadBatch = downloadBatchMap.get(batch.downloadBatchId());
        if (runningDownloadBatch != null) {
            return;
        }

        CallbackThrottle callbackThrottle = callbackThrottleCreator.create();

        DownloadBatch downloadBatch = DownloadBatchFactory.newInstance(
                batch,
                fileOperations,
                downloadsBatchPersistence,
                downloadsFilePersistence,
                callbackThrottle,
                connectionChecker
        );

        downloadBatch.persist();
        download(downloadBatch, downloadBatchMap);
    }

    public void download(DownloadBatch downloadBatch, Map<DownloadBatchId, DownloadBatch> downloadBatchMap) {
        downloadBatchMap.put(downloadBatch.getId(), downloadBatch);
        executor.submit(() -> Wait.<Void>waitFor(downloadService, waitForDownloadService)
                .thenPerform(executeDownload(downloadBatch)));
    }

    private Wait.ThenPerform.Action<Void> executeDownload(DownloadBatch downloadBatch) {
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

    private DownloadBatchStatusCallback downloadBatchCallback() {
        return downloadBatchStatus -> callbackHandler.post(() -> {
            try {
                semaphore.acquire();
                for (DownloadBatchStatusCallback callback : callbacks) {
                    callback.onUpdate(downloadBatchStatus);
                }
                notificationDispatcher.updateNotification(downloadBatchStatus);
                semaphore.release();
            } catch (InterruptedException e) {
                Log.e(e, "Interrupted whilst awaiting to acquire permit.");
            }
        });
    }

    void setDownloadService(DownloadService downloadService) {
        this.downloadService = downloadService;
        notificationDispatcher.setDownloadService(downloadService);
    }
}
