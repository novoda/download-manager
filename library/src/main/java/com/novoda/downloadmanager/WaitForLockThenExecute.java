package com.novoda.downloadmanager;

import com.novoda.notils.logger.simple.Log;

final class WaitForLockThenExecute {

    interface Action<T> {
        T performAction();
    }

    private WaitForLockThenExecute() {
        // Uses static factory method.
    }

    static <T> WaitForLockAndThenPerformAction<T> waitFor(Object lock) {
        return new WaitForLockAndThenPerformAction<>(lock);
    }

    static class WaitForLockAndThenPerformAction<T> {

        private final Object lock;

        WaitForLockAndThenPerformAction(Object lock) {
            this.lock = lock;
        }

        T thenPerform(final Action<T> action) {
            waitOnLock();
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
