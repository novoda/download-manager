package com.novoda.downloadmanager;

import java.io.File;

public interface DownloadMigrator {

    void startMigration(String jobIdentifier, File databaseFile, String basePath);

}
