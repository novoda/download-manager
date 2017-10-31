package com.novoda.downloadmanager;

import java.util.Timer;
import java.util.TimerTask;

class CallbackThrottleByTime implements CallbackThrottle {

    private final long period;

    private static final long DELAY = 0;

    private DownloadBatchStatus downloadBatchStatus;
    private Timer timer;
    private TimerTask timerTask;
    private DownloadBatchCallback callback;

    CallbackThrottleByTime(long period) {
        this.period = period;
    }

    @Override
    public void setCallback(final DownloadBatchCallback callback) {
        this.callback = callback;
        timerTask = new TimerTask() {
            @Override
            public void run() {
                callback.onUpdate(downloadBatchStatus);
            }
        };
    }

    @Override
    public void update(DownloadBatchStatus downloadBatchStatus) {
        if (timerTask == null) {
            return;
        }

        this.downloadBatchStatus = downloadBatchStatus;
        startUpdateIfNecessary();
    }

    private void startUpdateIfNecessary() {
        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(timerTask, DELAY, period);
        }
    }

    @Override
    public void stopUpdates() {
        if (callback != null) {
            callback.onUpdate(downloadBatchStatus);
        }

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
