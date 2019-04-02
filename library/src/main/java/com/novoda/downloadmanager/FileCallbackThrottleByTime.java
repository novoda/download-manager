package com.novoda.downloadmanager;

class FileCallbackThrottleByTime implements FileCallbackThrottle {

    private final ActionScheduler actionScheduler;

    private DownloadBatchStatus downloadBatchStatus;
    private DownloadBatchStatusCallback callback;

    FileCallbackThrottleByTime(ActionScheduler actionScheduler) {
        this.actionScheduler = actionScheduler;
    }

    @Override
    public void setCallback(final DownloadBatchStatusCallback callback) {
        this.callback = callback;
    }

    @Override
    public void update(DownloadBatchStatus downloadBatchStatus) {
        if (callback == null) {
            Logger.w("A DownloadBatchStatusCallback must be set before an update is called.");
            return;
        }

        this.downloadBatchStatus = downloadBatchStatus;

        if (!actionScheduler.isScheduled(action)) {
            actionScheduler.schedule(action);
        }
    }

    private final ActionScheduler.Action action = new ActionScheduler.Action() {
        @Override
        public void perform() {
            callback.onUpdate(downloadBatchStatus);
        }
    };

    @Override
    public void stopUpdates() {
        if (callback != null) {
            callback.onUpdate(downloadBatchStatus);
        }

        actionScheduler.cancelAll();
    }
}
