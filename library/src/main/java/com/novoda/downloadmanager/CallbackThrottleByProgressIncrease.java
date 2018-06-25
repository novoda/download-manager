package com.novoda.downloadmanager;

class CallbackThrottleByProgressIncrease implements CallbackThrottle {

    private DownloadBatchStatusCallback callback;
    private DownloadBatchStatus previousDownloadBatchStatus;
    private int currentProgress;

    @Override
    public void setCallback(DownloadBatchStatusCallback callback) {
        this.callback = callback;
    }

    @Override
    public void update(DownloadBatchStatus currentDownloadBatchStatus) {
        if (callback == null) {
            return;
        }

        if (matchesPreviousStatus(currentDownloadBatchStatus)) {
            return;
        }

        if (previousDownloadBatchStatus != null
                && errorUnchanged(currentDownloadBatchStatus)
                && progressUnchanged(currentDownloadBatchStatus)) {
            return;
        }

        callback.onUpdate(currentDownloadBatchStatus);
        this.previousDownloadBatchStatus = currentDownloadBatchStatus;
    }

    private boolean matchesPreviousStatus(DownloadBatchStatus currentDownloadBatchStatus) {
        return currentDownloadBatchStatus.equals(previousDownloadBatchStatus);
    }

    private boolean errorUnchanged(DownloadBatchStatus currentDownloadBatchStatus) {
        DownloadError previousError = previousDownloadBatchStatus.downloadError();
        DownloadError currentError = currentDownloadBatchStatus.downloadError();

        return currentError != null && currentError.equals(previousError);
    }

    private boolean progressUnchanged(DownloadBatchStatus currentDownloadBatchStatus) {
        return previousDownloadBatchStatus.percentageDownloaded() == currentDownloadBatchStatus.percentageDownloaded();
    }

    @Override
    public void stopUpdates() {
        callback.onUpdate(previousDownloadBatchStatus);
    }
}
