package com.novoda.downloadmanager;

public interface Migrator {

    void migrate();

    interface Callback {
        void onMigrationComplete();
    }

    Migrator NO_OP = new Migrator() {
        @Override
        public void migrate() {
            // no-op.
        }
    };
}
