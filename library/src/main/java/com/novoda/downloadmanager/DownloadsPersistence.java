package com.novoda.downloadmanager;

import java.util.List;

/**
 * For defining the persistence layer that the download-manager will use to
 * persist {@link DownloadBatch} and {@link BatchFile}.
 */
public interface DownloadsPersistence {

    void startTransaction();

    void endTransaction();

    void transactionSuccess();

    void persistBatch(DownloadsBatchPersisted batchPersisted);

    List<DownloadsBatchPersisted> loadBatches();

    void persistFile(DownloadsFilePersisted filePersisted);

    List<DownloadsFilePersisted> loadAllFiles();

    List<DownloadsFilePersisted> loadFiles(DownloadBatchId batchId);

    boolean delete(DownloadBatchId downloadBatchId);

    boolean update(DownloadBatchId downloadBatchId, DownloadBatchStatus.Status status);

    boolean update(DownloadBatchId downloadBatchId, boolean notificationSeen);

    void persistCompletedBatch(CompletedDownloadBatch completedDownloadBatch);
}
