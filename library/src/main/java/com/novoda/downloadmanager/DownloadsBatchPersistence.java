package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

class DownloadsBatchPersistence implements DownloadsBatchStatusPersistence {

    private final Executor executor;
    private final DownloadsFilePersistence downloadsFilePersistence;
    private final DownloadsPersistence downloadsPersistence;
    private final CallbackThrottleCreator callbackThrottleCreator;

    DownloadsBatchPersistence(Executor executor,
                              DownloadsFilePersistence downloadsFilePersistence,
                              DownloadsPersistence downloadsPersistence,
                              CallbackThrottleCreator callbackThrottleCreator) {
        this.executor = executor;
        this.downloadsFilePersistence = downloadsFilePersistence;
        this.downloadsPersistence = downloadsPersistence;
        this.callbackThrottleCreator = callbackThrottleCreator;
    }

    void persistAsync(final DownloadBatchTitle downloadBatchTitle,
                      final DownloadBatchId downloadBatchId,
                      final DownloadBatchStatus.Status status,
                      final List<DownloadFile> downloadFiles) {
        executor.execute(() -> {
            downloadsPersistence.startTransaction();

            try {
                LiteDownloadsBatchPersisted batchPersisted = new LiteDownloadsBatchPersisted(downloadBatchTitle, downloadBatchId, status);
                downloadsPersistence.persistBatch(batchPersisted);

                for (DownloadFile downloadFile : downloadFiles) {
                    downloadFile.persistSync();
                }

                downloadsPersistence.transactionSuccess();
            } finally {
                downloadsPersistence.endTransaction();
            }
        });
    }

    void loadAsync(final FileOperations fileOperations, final LoadBatchesCallback callback) {
        executor.execute(() -> {
            List<DownloadsBatchPersisted> batchPersistedList = downloadsPersistence.loadBatches();

            List<DownloadBatch> downloadBatches = new ArrayList<>(batchPersistedList.size());
            for (DownloadsBatchPersisted batchPersisted : batchPersistedList) {
                DownloadBatchStatus.Status status = batchPersisted.downloadBatchStatus();
                DownloadBatchId downloadBatchId = batchPersisted.downloadBatchId();
                DownloadBatchTitle downloadBatchTitle = batchPersisted.downloadBatchTitle();
                InternalDownloadBatchStatus liteDownloadBatchStatus = new LiteDownloadBatchStatus(
                        downloadBatchId,
                        downloadBatchTitle,
                        status
                );

                List<DownloadFile> downloadFiles = downloadsFilePersistence.loadSync(
                        downloadBatchId,
                        status,
                        fileOperations,
                        downloadsFilePersistence
                );

                Map<LiteDownloadFileId, Long> downloadedFileSizeMap = new HashMap<>(downloadFiles.size());

                long currentBytesDownloaded = 0;
                long totalBatchSizeBytes = 0;
                for (DownloadFile downloadFile : downloadFiles) {
                    downloadedFileSizeMap.put(downloadFile.id(), downloadFile.getCurrentDownloadedBytes());
                    currentBytesDownloaded += downloadFile.getCurrentDownloadedBytes();
                    totalBatchSizeBytes += downloadFile.getTotalSize();
                }

                liteDownloadBatchStatus.update(currentBytesDownloaded, totalBatchSizeBytes);

                CallbackThrottle callbackThrottle = callbackThrottleCreator.create();

                DownloadBatch downloadBatch = new DownloadBatch(
                        downloadBatchTitle,
                        downloadBatchId,
                        downloadFiles,
                        downloadedFileSizeMap,
                        liteDownloadBatchStatus,
                        DownloadsBatchPersistence.this,
                        callbackThrottle
                );

                downloadBatches.add(downloadBatch);
            }

            callback.onLoaded(downloadBatches);
        });
    }

    void deleteAsync(final DownloadBatchId downloadBatchId) {
        executor.execute(() -> {
            downloadsPersistence.startTransaction();
            try {
                downloadsPersistence.delete(downloadBatchId);
                downloadsPersistence.transactionSuccess();
            } finally {
                downloadsPersistence.endTransaction();
            }
        });
    }

    @Override
    public void updateStatusAsync(final DownloadBatchId downloadBatchId, final DownloadBatchStatus.Status status) {
        executor.execute(() -> {
            downloadsPersistence.startTransaction();
            try {
                downloadsPersistence.update(downloadBatchId, status);
                downloadsPersistence.transactionSuccess();
            } finally {
                downloadsPersistence.endTransaction();
            }
        });
    }

    interface LoadBatchesCallback {

        void onLoaded(List<DownloadBatch> downloadBatches);
    }
}
