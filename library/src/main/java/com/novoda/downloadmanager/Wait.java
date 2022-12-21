package com.novoda.downloadmanager;

import androidx.annotation.Nullable;

final class Wait {

    static class Criteria {
        @Nullable
        private ToWaitFor toWaitFor;

        void update(ToWaitFor toWaitFor) {
            this.toWaitFor = toWaitFor;
        }

        boolean isNotSatisfied() {
            return toWaitFor == null;
        }
    }

    private Wait() {
        // Uses static factory method.
    }

    static <T> ThenPerform<T> waitFor(Criteria criteria, Object lock) {
        return new ThenPerform<>(criteria, lock);
    }

    static class ThenPerform<T> {

        interface Action<T> {
            T performAction();
        }

        private final Criteria criteria;
        private final Object lock;

        ThenPerform(Criteria criteria, Object lock) {
            this.criteria = criteria;
            this.lock = lock;
        }

        T thenPerform(Action<T> action) {
            if (criteria.isNotSatisfied()) {
                waitForLock();
            }
            return action.performAction();
        }

        // Using simple object lock.
        @SuppressWarnings(value = "WA_NOT_IN_LOOP")
        private void waitForLock() {
            try {
                synchronized (lock) {
                    while (criteria.isNotSatisfied()) {
                        lock.wait();
                    }
                }
            } catch (InterruptedException e) {
                Logger.e(e, "Interrupted waiting for instance.");
            }
        }

    }

}
