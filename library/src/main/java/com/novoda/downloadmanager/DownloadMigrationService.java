package com.novoda.downloadmanager;

interface DownloadMigrationService extends DownloadManagerService {

    void startMigration(MigrationJobTemp migrationJob, MigrationCallback migrationCallback);
}
