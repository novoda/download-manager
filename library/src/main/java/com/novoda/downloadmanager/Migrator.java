package com.novoda.downloadmanager;

import android.support.annotation.WorkerThread;

public interface Migrator {

    @WorkerThread
    void migrate();

    interface Callback {
        void onUpdate(String message);
    }

    Migrator NO_OP = new Migrator() {
        @Override
        public void migrate() {
            // no-op.
        }
    };
}
