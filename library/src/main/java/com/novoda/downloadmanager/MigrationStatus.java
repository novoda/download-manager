package com.novoda.downloadmanager;

interface MigrationStatus {

    enum Status {

        EXTRACTING,
        MIGRATING_FILES,
        DELETING_V1_DATABASE,
        COMPLETE;

        public String toRawValue() {
            return this.name();
        }

    }

    int percentageMigrated();

    Status status();

}
