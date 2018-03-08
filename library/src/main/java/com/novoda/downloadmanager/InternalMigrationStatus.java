package com.novoda.downloadmanager;

interface InternalMigrationStatus extends MigrationStatus {

    void update(int numberOfMigrationsCompleted, int totalNumberOfMigrations);

    void migrationComplete();

    void markAsExtracting();

    void markAsMigrating();

    void markAsDeleting();

    void markAsComplete();

    VersionOneToVersionTwoMigrationStatus copy();

}
