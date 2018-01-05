package com.novoda.downloadmanager;

import com.novoda.notils.logger.simple.Log;

class WaitForServiceRunnable {

    interface Action {
        void performAction();
    }

    static ActionBuilder waitFor(Object lock) {
        return new ActionBuilder(lock);
    }

    static class ActionBuilder {

        private final Object lock;

        ActionBuilder(Object lock) {
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
