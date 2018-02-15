package com.novoda.downloadmanager;

import android.support.annotation.Nullable;

import com.novoda.notils.logger.simple.Log;

import java.util.concurrent.Callable;

final class Wait {

    private Wait() {
        // Uses static factory method.
    }

    static <T> ThenPerform<T> waitFor(@Nullable Object instanceToWaitFor, Object lock) {
        return new ThenPerform<>(instanceToWaitFor, lock);
    }

    static class ThenPerform<T> {

        private final Object instanceToWaitFor;
        private final Object lock;

        ThenPerform(Object instanceToWaitFor, Object lock) {
            this.instanceToWaitFor = instanceToWaitFor;
            this.lock = lock;
        }

        T thenPerform(Callable<T> callable) {
            if (instanceToWaitFor == null) {
                waitForLock();
            }

            try {
                return callable.call();
            } catch (Exception e) {
                throw new IllegalStateException("Cannot compute Callable result.", e);
            }
        }

        void thenPerform(Runnable runnable) {
            if (instanceToWaitFor == null) {
                waitForLock();
            }

            runnable.run();
        }

        private void waitForLock() {
            try {
                synchronized (lock) {
                    if (instanceToWaitFor == null) {
                        lock.wait();
                    }
                }
            } catch (InterruptedException e) {
                Log.e(e, "Interrupted waiting for instance.");
            }
        }

    }

}
