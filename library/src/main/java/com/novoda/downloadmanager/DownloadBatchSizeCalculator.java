package com.novoda.downloadmanager;

import androidx.annotation.WorkerThread;

import java.util.List;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DELETED;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DELETING;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.PAUSED;

final class DownloadBatchSizeCalculator {

    private DownloadBatchSizeCalculator() {
        // non instantiable
    }

    @WorkerThread
    static long getTotalSize(List<DownloadFile> downloadFiles, DownloadBatchStatus.Status status, DownloadBatchId downloadBatchId) {
        long totalBatchSize = 0;
        for (DownloadFile downloadFile : downloadFiles) {
            if (status == DELETING || status == DELETED || status == PAUSED) {
                Logger.w("abort getTotalSize file " + downloadFile.id().rawId()
                             + " from batch " + downloadBatchId.rawId()
                             + " with status " + status
                             + " returns 0 as totalFileSize");
                return 0;
            }

            long totalFileSize = downloadFile.getTotalSize();
            if (totalFileSize == 0) {
                Logger.w("file " + downloadFile.id().rawId()
                             + " from batch " + downloadBatchId.rawId()
                             + " with status " + status
                             + " returns 0 as totalFileSize");
                return 0;
            }

            totalBatchSize += totalFileSize;
        }
        return totalBatchSize;
    }
}
