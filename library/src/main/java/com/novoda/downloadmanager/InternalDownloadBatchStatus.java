package com.novoda.downloadmanager;

interface InternalDownloadBatchStatus extends DownloadBatchStatus {

    void updateTotalSize(long totalBatchSizeBytes);

    void updateDownloaded(long currentBytesDownloaded);

    void markAsDownloading(DownloadsBatchStatusPersistence persistence);

    void markAsPaused(DownloadsBatchStatusPersistence persistence);

    void markAsQueued(DownloadsBatchStatusPersistence persistence);

    void markAsDeleting();

    void markAsDeleted();

    void markAsError(Optional<DownloadError> downloadError, DownloadsBatchStatusPersistence persistence);

    void markAsDownloaded(DownloadsBatchStatusPersistence persistence);

    void markAsWaitingForNetwork(DownloadsBatchPersistence persistence);

    InternalDownloadBatchStatus copy();
}
