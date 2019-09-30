package com.novoda.downloadmanager;

import androidx.annotation.WorkerThread;

interface DownloadsBatchStatusPersistence {

    void updateStatusAsync(DownloadBatchId downloadBatchId, DownloadBatchStatus.Status status);

    @WorkerThread
    boolean persistCompletedBatch(CompletedDownloadBatch completedDownloadBatch);
}
