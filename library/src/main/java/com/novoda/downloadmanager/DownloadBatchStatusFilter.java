package com.novoda.downloadmanager;

class DownloadBatchStatusFilter {

    private int currentProgress;
    private DownloadBatchStatus.Status currentStatus;
    private DownloadError currentDownloadError;

    boolean shouldFilter(DownloadBatchStatus currentDownloadBatchStatus) {
        if (currentDownloadBatchStatus == null) {
            return true;
        }

        if (statusHasChanged(currentDownloadBatchStatus)
                || progressHasChanged(currentDownloadBatchStatus)
                || errorHasChanged(currentDownloadBatchStatus)) {

            currentStatus = currentDownloadBatchStatus.status();
            currentProgress = currentDownloadBatchStatus.percentageDownloaded();
            currentDownloadError = currentDownloadBatchStatus.downloadError();

            return false;
        }
        return true;
    }

    private boolean statusHasChanged(DownloadBatchStatus currentDownloadBatchStatus) {
        DownloadBatchStatus.Status newStatus = currentDownloadBatchStatus.status();
        return newStatus != null && !newStatus.equals(currentStatus);
    }

    private boolean progressHasChanged(DownloadBatchStatus currentDownloadBatchStatus) {
        int newProgress = currentDownloadBatchStatus.percentageDownloaded();
        return currentProgress != newProgress;
    }

    private boolean errorHasChanged(DownloadBatchStatus currentDownloadBatchStatus) {
        DownloadError newDownloadError = currentDownloadBatchStatus.downloadError();
        return newDownloadError != null && !newDownloadError.equals(currentDownloadError);
    }
}
