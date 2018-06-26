package com.novoda.downloadmanager;

class CallbackThrottleByProgressIncrease implements CallbackThrottle {

    private DownloadBatchStatusCallback callback;

    private int currentProgress;
    private DownloadBatchStatus.Status currentStatus;
    private DownloadError currentDownloadError;

    @Override
    public void setCallback(DownloadBatchStatusCallback callback) {
        this.callback = callback;
    }

    @Override
    public void update(DownloadBatchStatus currentDownloadBatchStatus) {
        if (callback == null) {
            return;
        }

        if (statusHasChanged(currentDownloadBatchStatus)
                || progressHasChanged(currentDownloadBatchStatus)
                || errorHasChanged(currentDownloadBatchStatus)) {

            currentStatus = currentDownloadBatchStatus.status();
            currentProgress = currentDownloadBatchStatus.percentageDownloaded();
            currentDownloadError = currentDownloadBatchStatus.downloadError();

            callback.onUpdate(currentDownloadBatchStatus);
        }
    }

    private boolean statusHasChanged(DownloadBatchStatus currentDownloadBatchStatus) {
        DownloadBatchStatus.Status newStatus = currentDownloadBatchStatus.status();
        return !currentStatus.equals(newStatus);
    }

    private boolean progressHasChanged(DownloadBatchStatus currentDownloadBatchStatus) {
        int newProgress = currentDownloadBatchStatus.percentageDownloaded();
        return currentProgress != newProgress;
    }

    private boolean errorHasChanged(DownloadBatchStatus currentDownloadBatchStatus) {
        DownloadError newDownloadError = currentDownloadBatchStatus.downloadError();
        return !currentDownloadError.equals(newDownloadError);
    }

    @Override
    public void stopUpdates() {
        callback = null;
    }
}
