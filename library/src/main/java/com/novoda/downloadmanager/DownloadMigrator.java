package com.novoda.downloadmanager;

public interface DownloadMigrator {

    void startMigration(String databaseFilename, MigrationCallback migrationCallback);

}
