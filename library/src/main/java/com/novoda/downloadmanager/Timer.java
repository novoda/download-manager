package com.novoda.downloadmanager;

import android.os.Process;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

class Timer {

    private final DownloadsHandler downloadsHandler;

    private Callback callback;

    public Timer(DownloadsHandler downloadsHandler) {
        this.downloadsHandler = downloadsHandler;
    }

    public void scheduleNow(Callback callback) {
        this.callback = callback;

        downloadsHandler.removeCallbacks(updateCallback);
        downloadsHandler.post(updateCallback);
    }

    public void scheduleLater(Callback callback) {
        this.callback = callback;

        downloadsHandler.removeCallbacks(updateCallback);
        downloadsHandler.postDelayed(updateCallback, 5 * MINUTE_IN_MILLIS);
    }

    private final Runnable updateCallback = new Runnable() {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            callback.onUpdate();
        }
    };

    public void release() {
        downloadsHandler.removeCallbacks(updateCallback);
        downloadsHandler.stop();
    }

    interface Callback {
        void onUpdate();
    }

}
