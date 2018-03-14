package com.novoda.downloadmanager;

import com.novoda.notils.logger.simple.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

class DownloadsBatchPersistence implements DownloadsBatchStatusPersistence, DownloadsNotificationSeenPersistence {

    private final Executor executor;
    private final DownloadsFilePersistence downloadsFilePersistence;
    private final DownloadsPersistence downloadsPersistence;
    private final CallbackThrottleCreator callbackThrottleCreator;
    private final ConnectionChecker connectionChecker;

    DownloadsBatchPersistence(Executor executor,
                              DownloadsFilePersistence downloadsFilePersistence,
                              DownloadsPersistence downloadsPersistence,
                              CallbackThrottleCreator callbackThrottleCreator,
                              ConnectionChecker connectionChecker) {
        this.executor = executor;
        this.downloadsFilePersistence = downloadsFilePersistence;
        this.downloadsPersistence = downloadsPersistence;
        this.callbackThrottleCreator = callbackThrottleCreator;
        this.connectionChecker = connectionChecker;
    }

    void persistAsync(DownloadBatchTitle downloadBatchTitle,
                      DownloadBatchId downloadBatchId,
                      DownloadBatchStatus.Status status,
                      List<DownloadFile> downloadFiles,
                      long downloadedDateTimeInMillis,
                      boolean notificationSeen) {
        executor.execute(() -> {
            List<DownloadFile> downloadFilesToPersist = new ArrayList<>(downloadFiles);
            Log.v("start DownloadBatchPersistence persist full batch " + downloadBatchId + ", downloadFilesSize: " + downloadFilesToPersist.size());
            downloadsPersistence.startTransaction();

            try {
                LiteDownloadsBatchPersisted batchPersisted = new LiteDownloadsBatchPersisted(
                        downloadBatchTitle,
                        downloadBatchId,
                        status,
                        downloadedDateTimeInMillis,
                        notificationSeen
                );
                downloadsPersistence.persistBatch(batchPersisted);
                for (DownloadFile downloadFile : downloadFilesToPersist) {
                    downloadFile.persistSync();
                }
                downloadsPersistence.transactionSuccess();
            } finally {
                downloadsPersistence.endTransaction();
            }
            Log.v("end DownloadBatchPersistence persist full batch " + downloadBatchId);
        });
    }

    void loadAsync(FileOperations fileOperations, LoadBatchesCallback callback) {
        executor.execute(() -> {
            List<DownloadsBatchPersisted> batchPersistedList = downloadsPersistence.loadBatches();

            List<DownloadBatch> downloadBatches = new ArrayList<>(batchPersistedList.size());
            for (DownloadsBatchPersisted batchPersisted : batchPersistedList) {
                DownloadBatchStatus.Status status = batchPersisted.downloadBatchStatus();
                DownloadBatchId downloadBatchId = batchPersisted.downloadBatchId();
                DownloadBatchTitle downloadBatchTitle = batchPersisted.downloadBatchTitle();
                long downloadedDateTimeInMillis = batchPersisted.downloadedDateTimeInMillis();
                boolean notificationSeen = batchPersisted.notificationSeen();
                InternalDownloadBatchStatus liteDownloadBatchStatus = new LiteDownloadBatchStatus(
                        downloadBatchId,
                        downloadBatchTitle,
                        downloadedDateTimeInMillis,
                        status,
                        notificationSeen
                );

                List<DownloadFile> downloadFiles = downloadsFilePersistence.loadSync(
                        downloadBatchId,
                        status,
                        fileOperations,
                        downloadsFilePersistence
                );

                downloadFiles = Collections.unmodifiableList(downloadFiles);

                Map<DownloadFileId, Long> downloadedFileSizeMap = new HashMap<>(downloadFiles.size());

                long currentBytesDownloaded = 0;
                long totalBatchSizeBytes = 0;
                for (DownloadFile downloadFile : downloadFiles) {
                    downloadedFileSizeMap.put(downloadFile.id(), downloadFile.getCurrentDownloadedBytes());
                    currentBytesDownloaded += downloadFile.getCurrentDownloadedBytes();
                    long totalFileSize = downloadFile.getTotalSize();
                    if (totalFileSize == 0) {
                        totalBatchSizeBytes = 0;
                        currentBytesDownloaded = 0;
                        break;
                    } else {
                        totalBatchSizeBytes += totalFileSize;
                    }
                }

                liteDownloadBatchStatus.update(currentBytesDownloaded, totalBatchSizeBytes);

                CallbackThrottle callbackThrottle = callbackThrottleCreator.create();

                DownloadBatch downloadBatch = new DownloadBatch(
                        liteDownloadBatchStatus,
                        downloadFiles,
                        downloadedFileSizeMap,
                        DownloadsBatchPersistence.this,
                        callbackThrottle,
                        connectionChecker
                );

                downloadBatches.add(downloadBatch);
            }

            callback.onLoaded(downloadBatches);
        });
    }

    void deleteAsync(DownloadBatchId downloadBatchId) {
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
    public void updateStatusAsync(DownloadBatchId downloadBatchId, DownloadBatchStatus.Status status) {
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

    @Override
    public void updateNotificationSeenAsync(DownloadBatchStatus downloadBatchStatus, boolean notificationSeen) {
        executor.execute(() -> {
            Log.v("start updateNotificationSeenAsync with: " + downloadBatchStatus + ", notificationSeen: " + notificationSeen);
            if (downloadBatchStatus.status() == DownloadBatchStatus.Status.DOWNLOADED) {
                downloadsPersistence.startTransaction();
                try {
                    downloadsPersistence.update(downloadBatchStatus.getDownloadBatchId(), notificationSeen);
                    downloadsPersistence.transactionSuccess();
                } finally {
                    downloadsPersistence.endTransaction();
                }
            }
            Log.v("end updateNotificationSeenAsync with: " + downloadBatchStatus + ", notificationSeen: " + notificationSeen);
        });
    }

    interface LoadBatchesCallback {

        void onLoaded(List<DownloadBatch> downloadBatches);
    }
}
