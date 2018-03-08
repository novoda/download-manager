package com.novoda.downloadmanager;

public interface MigrationStatus {

    enum Status {

        DB_NOT_PRESENT,
        EXTRACTING,
        MIGRATING_FILES,
        DELETING_V1_DATABASE,
        COMPLETE;

        public String toRawValue() {
            return this.name();
        }

    }

    String migrationId();

    int numberOfMigratedBatches();

    int totalNumberOfBatchesToMigrate();

    int percentageMigrated();

    Status status();

}
