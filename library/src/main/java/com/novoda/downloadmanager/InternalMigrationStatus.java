package com.novoda.downloadmanager;

interface InternalMigrationStatus extends MigrationStatus {

    void migrationComplete();

    void markAsExtracting();

    void markAsMigrating();

    void markAsDeleting();

    void markAsComplete();

    InternalMigrationStatus copy();

}
