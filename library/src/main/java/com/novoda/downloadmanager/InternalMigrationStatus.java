package com.novoda.downloadmanager;

interface InternalMigrationStatus extends MigrationStatus {

    void onSingleBatchMigrated();

    void markAsExtracting();

    void markAsMigrating();

    void markAsDeleting();

    void markAsComplete();

    InternalMigrationStatus copy();

}
