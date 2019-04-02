package com.novoda.downloadmanager;

class FileCallbackThrottleByProgressIncrease implements FileCallbackThrottle {

    private DownloadBatchStatusCallback callback;

    private int currentProgress;

    @Override
    public void setCallback(DownloadBatchStatusCallback callback) {
        this.callback = callback;
    }

    @Override
    public void update(DownloadBatchStatus currentDownloadBatchStatus) {
        if (callback == null) {
            Logger.w("A DownloadBatchStatusCallback must be set before an update is called.");
            return;
        }

        if (progressHasChanged(currentDownloadBatchStatus)) {
            currentProgress = currentDownloadBatchStatus.percentageDownloaded();
            callback.onUpdate(currentDownloadBatchStatus);
        }
    }

    private boolean progressHasChanged(DownloadBatchStatus currentDownloadBatchStatus) {
        int newProgress = currentDownloadBatchStatus.percentageDownloaded();
        return currentProgress != newProgress;
    }

    @Override
    public void stopUpdates() {
        callback = null;
    }
}
