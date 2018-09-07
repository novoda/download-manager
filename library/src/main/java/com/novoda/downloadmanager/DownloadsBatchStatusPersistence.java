package com.novoda.downloadmanager;

import android.support.annotation.WorkerThread;

interface DownloadsBatchStatusPersistence {

    void updateStatusAsync(DownloadBatchId downloadBatchId, DownloadBatchStatus.Status status);

    @WorkerThread
    boolean persistCompletedBatch(CompletedDownloadBatch completedDownloadBatch);
}
