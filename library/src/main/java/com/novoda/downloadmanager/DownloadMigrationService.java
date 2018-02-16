package com.novoda.downloadmanager;

interface DownloadMigrationService extends DownloadManagerService {

    void startMigration(MigrationJob migrationJob, MigrationCallback migrationCallback);
}
