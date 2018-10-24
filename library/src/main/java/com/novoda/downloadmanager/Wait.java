package com.novoda.downloadmanager;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

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

        @SuppressWarnings(value = "WA_NOT_IN_LOOP", justification = "Using simple object lock.")
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
