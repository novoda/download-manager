package com.novoda.downloadmanager;

import java.util.List;

public interface DownloadMigrator {

    void startMigration(String jobIdentifier, List<Migration> partialMigrations, List<Migration> completeMigrations);

}
