package com.novoda.downloadmanager;

interface InternalMigrationStatus extends MigrationStatus {

    void update(int currentBatch, int numberOfBatches);

    void markAsExtracting();

    void markAsMigrating();

    void markAsDeleting();

    void markAsComplete();

}
