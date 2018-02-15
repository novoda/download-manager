package com.novoda.downloadmanager;

import android.support.annotation.Nullable;

import com.novoda.notils.logger.simple.Log;

final class Wait {

    private Wait() {
        // Uses static factory method.
    }

    static <T> ThenPerform<T> waitFor(@Nullable ToWaitFor instance, Object lock) {
        return new ThenPerform<>(instance, lock);
    }

    static class ThenPerform<T> {

        interface Action<T> {
            T performAction();
        }

        private final ToWaitFor instance;
        private final Object lock;

        ThenPerform(ToWaitFor instance, Object lock) {
            this.instance = instance;
            this.lock = lock;
        }

        T thenPerform(Action<T> action) {
            if (instance == null) {
                waitForLock();
            }
            return action.performAction();
        }

        private void waitForLock() {
            try {
                synchronized (lock) {
                    if (instance == null) {
                        lock.wait();
                    }
                }
            } catch (InterruptedException e) {
                Log.e(e, "Interrupted waiting for :", lock.getClass().getSimpleName());
            }
        }

    }

}
