package com.novoda.downloadmanager;

import com.novoda.notils.logger.simple.Log;

final class WaitForLockRunnable {

    interface Action {
        void performAction();
    }

    private WaitForLockRunnable() {
        // Uses static factory method.
    }

    static WaitForLockAndThenPerformActionRunnable waitFor(Object lock) {
        return new WaitForLockAndThenPerformActionRunnable(lock);
    }

    static class WaitForLockAndThenPerformActionRunnable {

        private final Object lock;

        WaitForLockAndThenPerformActionRunnable(Object lock) {
            this.lock = lock;
        }

        Runnable thenPerform(final Action action) {
            return new Runnable() {
                @Override
                public void run() {
                    waitOnLock();
                    action.performAction();
                }
            };
        }

        private void waitOnLock() {
            try {
                synchronized (lock) {
                    lock.wait();
                }
            } catch (InterruptedException e) {
                Log.e(e, "Interruped waiting for download service.");
            }
        }
    }

}
