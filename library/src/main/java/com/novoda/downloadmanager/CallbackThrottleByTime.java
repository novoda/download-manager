package com.novoda.downloadmanager;

class CallbackThrottleByTime implements CallbackThrottle {

    private final Scheduler scheduler;

    private DownloadBatchStatus downloadBatchStatus;
    private DownloadBatchCallback callback;

    CallbackThrottleByTime(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void setCallback(final DownloadBatchCallback callback) {
        this.callback = callback;
    }

    @Override
    public void update(DownloadBatchStatus downloadBatchStatus) {
        if (callback == null) {
            return;
        }

        this.downloadBatchStatus = downloadBatchStatus;

        if (!scheduler.isScheduled(action)) {
            scheduler.schedule(action);
        }
    }

    private final Scheduler.Action action = new Scheduler.Action() {
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

        scheduler.cancelAll();
    }
}
