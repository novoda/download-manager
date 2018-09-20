package com.novoda.downloadmanager;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

final class Wait {

    static class Holder {
        @Nullable
        private ToWaitFor toWaitFor;

        void update(ToWaitFor toWaitFor) {
            this.toWaitFor = toWaitFor;
        }

        ToWaitFor toWaitFor() {
            return toWaitFor;
        }
    }

    private Wait() {
        // Uses static factory method.
    }

    static <T> ThenPerform<T> waitFor(Holder holder, Object lock) {
        return new ThenPerform<>(holder, lock);
    }

    static class ThenPerform<T> {

        interface Action<T> {
            T performAction();
        }

        private final Holder holder;
        private final Object lock;

        ThenPerform(Holder holder, Object lock) {
            this.holder = holder;
            this.lock = lock;
        }

        T thenPerform(Action<T> action) {
            if (holder.toWaitFor() == null) {
                waitForLock();
            }
            return action.performAction();
        }

        @SuppressWarnings(value = "WA_NOT_IN_LOOP", justification = "Using simple object lock.")
        private void waitForLock() {
            try {
                synchronized (lock) {
                    while (holder.toWaitFor() == null) {
                        lock.wait();
                    }
                }
            } catch (InterruptedException e) {
                Logger.e(e, "Interrupted waiting for instance.");
            }
        }

    }

}
