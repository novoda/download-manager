package com.novoda.downloadmanager;

import android.support.annotation.Nullable;

import com.novoda.notils.logger.simple.Log;

final class WaitForDownloadService {

    private WaitForDownloadService() {
        // Uses static factory method.
    }

    static <T> ThenPerform<T> waitFor(@Nullable DownloadService downloadService, Object downloadServiceLock) {
        return new ThenPerform<>(downloadService, downloadServiceLock);
    }

    static class ThenPerform<T> {

        interface Action<T> {
            T performAction();
        }

        private final DownloadService downloadService;
        private final Object downloadServiceLock;

        ThenPerform(DownloadService downloadService, Object downloadServiceLock) {
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
