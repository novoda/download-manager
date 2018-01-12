package com.novoda.downloadmanager;

import android.support.annotation.Nullable;

import com.novoda.notils.logger.simple.Log;

final class WaitForDownloadServiceThenPerform {

    interface Action<T> {
        T performAction();
    }

    private WaitForDownloadServiceThenPerform() {
        // Uses static factory method.
    }

    static <T> WaitForDownloadServiceThenPerformAction<T> waitFor(@Nullable DownloadService downloadService, Object downloadServiceLock) {
        return new WaitForDownloadServiceThenPerformAction<>(downloadService, downloadServiceLock);
    }

    static class WaitForDownloadServiceThenPerformAction<T> {

        private final DownloadService downloadService;
        private final Object lock;

        WaitForDownloadServiceThenPerformAction(DownloadService downloadService, Object lock) {
            this.downloadService = downloadService;
            this.lock = lock;
        }

        T thenPerform(final Action<T> action) {
            if (downloadService == null) {
                try {
                    synchronized (lock) {
                        lock.wait();
                    }
                } catch (InterruptedException e) {
                    Log.e(e, "Interrupted waiting for download service.");
                }
            }
            return action.performAction();
        }

    }

}
