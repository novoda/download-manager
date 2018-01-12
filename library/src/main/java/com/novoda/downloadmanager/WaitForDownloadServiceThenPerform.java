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
        private final Object downloadServiceLock;

        WaitForDownloadServiceThenPerformAction(DownloadService downloadService, Object downloadServiceLock) {
            this.downloadService = downloadService;
            this.downloadServiceLock = downloadServiceLock;
        }

        T thenPerform(Action<T> action) {
            if (downloadService == null) {
                waitForLock();
            }
            return action.performAction();
        }

        private void waitForLock() {
            try {
                synchronized (downloadServiceLock) {
                    if (downloadService == null) {
                        downloadServiceLock.wait();
                    }
                }
            } catch (InterruptedException e) {
                Log.e(e, "Interrupted waiting for download service.");
            }
        }

    }

}
