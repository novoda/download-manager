package com.novoda.downloadmanager;

class CallbackThrottleByTime implements CallbackThrottle {

    private final ActionScheduler actionScheduler;

    private DownloadBatchStatus downloadBatchStatus;
    private DownloadBatchStatusCallback callback;

    CallbackThrottleByTime(ActionScheduler actionScheduler) {
        this.actionScheduler = actionScheduler;
    }

    @Override
    public void setCallback(final DownloadBatchStatusCallback callback) {
        this.callback = callback;
    }

    @Override
    public void update(DownloadBatchStatus downloadBatchStatus) {
        if (callback == null) {
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
