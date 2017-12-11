package com.novoda.downloadmanager;

public interface Migrator {

    void migrate();

    interface Callback {
        void onMigrationComplete();

        void onUpdate(String message);
    }

    Migrator NO_OP = new Migrator() {
        @Override
        public void migrate() {
            // no-op.
        }
    };
}
