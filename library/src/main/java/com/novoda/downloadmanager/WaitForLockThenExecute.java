package com.novoda.downloadmanager;

import android.support.annotation.Nullable;

import com.novoda.notils.logger.simple.Log;

final class WaitForLockThenExecute {

    interface Action<T> {
        T performAction();
    }

    private WaitForLockThenExecute() {
        // Uses static factory method.
    }

    static <T> WaitForLockAndThenPerformAction<T> waitFor(@Nullable DownloadService downloadService, Object lock) {
        return new WaitForLockAndThenPerformAction<>(downloadService, lock);
    }

    static class WaitForLockAndThenPerformAction<T> {

        private final DownloadService downloadService;
        private final Object lock;

        WaitForLockAndThenPerformAction(DownloadService downloadService, Object lock) {
            this.downloadService = downloadService;
            this.lock = lock;
        }

        T thenPerform(final Action<T> action) {
            if (downloadService == null) {
                waitOnLock();
            }
            return action.performAction();
        }

        private void waitOnLock() {
            try {
                synchronized (lock) {
                    lock.wait();
                }
            } catch (InterruptedException e) {
                Log.e(e, "Interrupted waiting for download service.");
            }
        }
    }

}
