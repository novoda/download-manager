package com.novoda.downloadmanager;

public interface Migrator {

    void migrate();

    boolean isRunning();

    interface Callback {
        void onUpdate(String message);
    }

    Migrator NO_OP = new Migrator() {
        @Override
        public void migrate() {
            // no-op.
        }

        @Override
        public boolean isRunning() {
            return false;
        }
    };
}
