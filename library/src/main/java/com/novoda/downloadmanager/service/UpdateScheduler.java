package com.novoda.downloadmanager.service;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

class UpdateScheduler {

    private final HandlerThread updateThread;
    private final Handler updateHandler;

    private OnUpdate onUpdate;

    UpdateScheduler(HandlerThread updateThread, Handler updateHandler) {
        this.updateThread = updateThread;
        this.updateHandler = updateHandler;
    }

    public void scheduleNow(OnUpdate onUpdate) {
        this.onUpdate = onUpdate;
        updateHandler.removeCallbacks(updateCallback);
        updateHandler.post(updateCallback);
    }

    public void scheduleLater(OnUpdate onUpdate) {
        this.onUpdate = onUpdate;
        updateHandler.removeCallbacks(updateCallback);
        updateHandler.postDelayed(updateCallback, 5 * MINUTE_IN_MILLIS);
    }

    private final Runnable updateCallback = new Runnable() {
        @Override
        public void run() {
            Log.e("!!!", "update callback triggered");
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            onUpdate.onUpdate();
        }
    };

    public void release() {
        updateHandler.removeCallbacks(updateCallback);
        updateThread.quit();
    }

    interface OnUpdate {
        void onUpdate();
    }

}
