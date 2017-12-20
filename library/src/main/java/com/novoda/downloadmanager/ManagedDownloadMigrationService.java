package com.novoda.downloadmanager;

public interface ManagedDownloadMigrationService extends DownloadMigrationService {
    MigrationFuture startMigration();
}
