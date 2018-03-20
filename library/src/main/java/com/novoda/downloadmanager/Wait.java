package com.novoda.downloadmanager;

import android.support.annotation.Nullable;

final class Wait {

    private Wait() {
        // Uses static factory method.
    }

    static <T> ThenPerform<T> waitFor(@Nullable ToWaitFor instanceToWaitFor, Object lock) {
        return new ThenPerform<>(instanceToWaitFor, lock);
    }

    static class ThenPerform<T> {

        interface Action<T> {
            T performAction();
        }

        private final ToWaitFor instanceToWaitFor;
        private final Object lock;

        ThenPerform(ToWaitFor instanceToWaitFor, Object lock) {
            this.instanceToWaitFor = instanceToWaitFor;
            this.lock = lock;
        }

        T thenPerform(Action<T> action) {
            if (instanceToWaitFor == null) {
                waitForLock();
            }
            return action.performAction();
        }

        private void waitForLock() {
            try {
                synchronized (lock) {
                    if (instanceToWaitFor == null) {
                        lock.wait();
                    }
                }
            } catch (InterruptedException e) {
                Logger.e(e, "Interrupted waiting for instance.");
            }
        }

    }

}
