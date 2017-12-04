package com.novoda.downloadmanager;

public interface Migrator {
    void migrate();

    interface Callback {
        void onMigrationComplete();
    }
}
