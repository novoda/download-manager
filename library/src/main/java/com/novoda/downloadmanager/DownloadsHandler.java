package com.novoda.downloadmanager;

import android.os.Handler;
import android.os.HandlerThread;

final class DownloadsHandler {

    private final Handler handler;
    private final HandlerThread handlerThread;

    public static DownloadsHandler start() {
        HandlerThread updateThread = new HandlerThread("DownloadManager-UpdateThread");
        updateThread.start();
        Handler updateHandler = new Handler(updateThread.getLooper());
        return new DownloadsHandler(updateHandler, updateThread);
    }

    private DownloadsHandler(Handler handler, HandlerThread handlerThread) {
        this.handler = handler;
        this.handlerThread = handlerThread;
    }

    public Handler getHandler() {
        return handler;
    }

    public void post(Runnable runnable) {
        handler.post(runnable);
    }

    public void postDelayed(Runnable runnable, long delayMillis) {
        handler.postDelayed(runnable, delayMillis);
    }

    public void removeCallbacks(Runnable runnable) {
        handler.removeCallbacks(runnable);
    }

    public void stop() {
        handlerThread.quit();
    }
}
