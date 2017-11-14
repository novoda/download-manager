package com.novoda.downloadmanager;

import android.os.Handler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status.*;

class LiteDownloadManagerDownloader {

    private final Object waitForDownloadService;
    private final ExecutorService executor;
    private final Handler callbackHandler;
    private final FileOperations fileOperations;
    private final DownloadsBatchPersistence downloadsBatchPersistence;
    private final DownloadsFilePersistence downloadsFilePersistence;
    private final NotificationCreator notificationCreator;
    private final List<DownloadBatchCallback> callbacks;
    private final CallbackThrottleCreator callbackThrottleCreator;

    private DownloadService downloadService;

    LiteDownloadManagerDownloader(Object waitForDownloadService,
                                  ExecutorService executor,
                                  Handler callbackHandler,
                                  FileOperations fileOperations,
                                  DownloadsBatchPersistence downloadsBatchPersistence,
                                  DownloadsFilePersistence downloadsFilePersistence,
                                  NotificationCreator notificationCreator,
                                  List<DownloadBatchCallback> callbacks,
                                  CallbackThrottleCreator callbackThrottleCreator) {
        this.waitForDownloadService = waitForDownloadService;
        this.executor = executor;
        this.callbackHandler = callbackHandler;
        this.fileOperations = fileOperations;
        this.downloadsBatchPersistence = downloadsBatchPersistence;
        this.downloadsFilePersistence = downloadsFilePersistence;
        this.notificationCreator = notificationCreator;
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
        if (downloadService == null) {
            ensureDownloadServiceExistsAndDownload(downloadBatch);
        } else {
            executeDownload(downloadBatch);
        }
    }

    private void ensureDownloadServiceExistsAndDownload(final DownloadBatch downloadBatch) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                waitForDownloadService();
                executeDownload(downloadBatch);
            }
        });
    }

    private void waitForDownloadService() {
        try {
            synchronized (waitForDownloadService) {
                if (downloadService == null) {
                    waitForDownloadService.wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void executeDownload(final DownloadBatch downloadBatch) {
        InternalDownloadBatchStatus downloadBatchStatus = downloadBatch.status();
        if (downloadBatchStatus.status() != DOWNLOADED) {
            updateStatusToQueuedIfNeeded(downloadBatchStatus);
            downloadService.download(downloadBatch, downloadBatchCallback());
        }
    }

    private void updateStatusToQueuedIfNeeded(InternalDownloadBatchStatus downloadBatchStatus) {
        if (downloadBatchStatus.status() != PAUSED) {
            downloadBatchStatus.markAsQueued(downloadsBatchPersistence);
        }
    }

    private DownloadBatchCallback downloadBatchCallback() {
        return new DownloadBatchCallback() {
            @Override
            public void onUpdate(final DownloadBatchStatus downloadBatchStatus) {
                callbackHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (DownloadBatchCallback callback : callbacks) {
                            callback.onUpdate(downloadBatchStatus);
                        }
                        updateNotification(downloadBatchStatus, downloadService);
                    }
                });
            }
        };
    }

    private void updateNotification(DownloadBatchStatus liteDownloadBatchStatus, DownloadService downloadService) {
        NotificationInformation notificationInformation = notificationCreator.createNotification(
                liteDownloadBatchStatus.getDownloadBatchTitle(),
                liteDownloadBatchStatus.percentageDownloaded(),
                (int) liteDownloadBatchStatus.bytesTotalSize(),
                (int) liteDownloadBatchStatus.bytesDownloaded()
        );

        if (liteDownloadBatchStatus.status() == DELETION) {
            downloadService.dismissNotification();
            return;
        }

        if (liteDownloadBatchStatus.status() == DOWNLOADED) {
            downloadService.stackNotification(notificationInformation);
            return;
        }

        downloadService.updateNotification(notificationInformation);
    }

    void setDownloadService(DownloadService downloadService) {
        this.downloadService = downloadService;
    }
}
