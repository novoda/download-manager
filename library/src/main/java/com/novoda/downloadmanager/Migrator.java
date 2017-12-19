package com.novoda.downloadmanager;

import android.support.annotation.WorkerThread;

interface Migrator {

    @WorkerThread
    void migrate();

    Migrator NO_OP = new Migrator() {
        @Override
        public void migrate() {
            // no-op.
        }
    };
}
