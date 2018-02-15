package com.novoda.downloadmanager;

import android.content.Context;

import java.io.File;

class LiteDownloadMigrator implements DownloadMigrator {

    private final File databaseFile;
    private final Object waitForMigrationService;

    private DownloadMigrationService migrationService;

    private final Context applicationContext;

    LiteDownloadMigrator(Context context, File databaseFile, Object waitForMigrationService) {
        this.applicationContext = context.getApplicationContext();
        this.databaseFile = databaseFile;
        this.waitForMigrationService = waitForMigrationService;
    }

    void initialise(DownloadMigrationService migrationService) {
        this.migrationService = migrationService;
        // migrationJob.setDownloadService.
        synchronized (waitForMigrationService) {
            waitForMigrationService.notifyAll();
        }
    }

    @Override
    public void startMigration() {
        Wait.waitFor(migrationService, waitForMigrationService)
                .thenPerform(() -> {
                    // Pass db and callback? This can forward to UI or NotificationDispatcher.
                    migrationService.startMigration(new MigrationJob(applicationContext, databaseFile));
                    return null;
                });
    }

}
