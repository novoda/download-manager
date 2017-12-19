package com.novoda.downloadmanager;

public interface MigrationStatus {

    enum Status {

        EXTRACTING,
        MIGRATING_FILES,
        DELETING_V1_DATABASE,
        COMPLETE;

        public String toRawValue() {
            return this.name();
        }

    }

    int numberOfMigratedBatches();

    int totalNumberOfBatchesToMigrate();

    int percentageMigrated();

    Status status();

}
