package com.novoda.downloadmanager.service;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

public class Timer {

    private final HandlerThread updateThread;
    private final Handler updateHandler;

    private Callback callback;

    public Timer(HandlerThread updateThread, Handler updateHandler) {
        this.updateThread = updateThread;
        this.updateHandler = updateHandler;
    }

    public void scheduleNow(Callback callback) {
        this.callback = callback;

        updateHandler.removeCallbacks(updateCallback);
        updateHandler.post(updateCallback);
    }

    public void scheduleLater(Callback callback) {
        this.callback = callback;

        updateHandler.removeCallbacks(updateCallback);
        updateHandler.postDelayed(updateCallback, 5 * MINUTE_IN_MILLIS);
    }

    private final Runnable updateCallback = new Runnable() {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            callback.onUpdate();
        }
    };

    public void release() {
        updateHandler.removeCallbacks(updateCallback);
        updateThread.quit();
    }

    interface Callback {
        void onUpdate();
    }

}
