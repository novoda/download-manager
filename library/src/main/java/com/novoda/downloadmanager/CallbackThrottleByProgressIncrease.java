package com.novoda.downloadmanager;

class CallbackThrottleByProgressIncrease implements CallbackThrottle {

    private DownloadBatchCallback callback;
    private DownloadBatchStatus downloadBatchStatus;
    private int currentProgress;

    @Override
    public void setCallback(DownloadBatchCallback callback) {
        this.callback = callback;
    }

    @Override
    public void update(DownloadBatchStatus downloadBatchStatus) {
        if (callback == null) {
            return;
        }

        this.downloadBatchStatus = downloadBatchStatus;

        if (progressIncreased(downloadBatchStatus)) {
            callback.onUpdate(downloadBatchStatus);
        }
    }

    private boolean progressIncreased(DownloadBatchStatus downloadBatchStatus) {
        int newProgress = downloadBatchStatus.percentageDownloaded();

        if (currentProgress == newProgress) {
            return false;
        } else {
            currentProgress = newProgress;
            return true;
        }
    }

    @Override
    public void stopUpdates() {
        callback.onUpdate(downloadBatchStatus);
    }
}
