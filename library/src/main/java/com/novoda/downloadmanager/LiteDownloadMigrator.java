package com.novoda.downloadmanager;

import android.content.Context;
import android.os.Handler;

import java.io.File;
import java.util.concurrent.ExecutorService;

class LiteDownloadMigrator implements DownloadMigrator {

    private final File databaseFile;
    private final Object waitForMigrationService;
    private final ExecutorService executor;
    private final Handler callbackHandler;

    private DownloadMigrationService migrationService;

    private final Context applicationContext;

    LiteDownloadMigrator(Context context,
                         File databaseFile,
                         Object waitForMigrationService,
                         ExecutorService executor,
                         Handler callbackHandler) {
        this.applicationContext = context.getApplicationContext();
        this.databaseFile = databaseFile;
        this.waitForMigrationService = waitForMigrationService;
        this.executor = executor;
        this.callbackHandler = callbackHandler;
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
        executor.submit(() -> Wait.<Void>waitFor(migrationService, waitForMigrationService)
                .thenPerform(executeMigration()));
    }

    private Wait.ThenPerform.Action<Void> executeMigration() {
        return () -> {
            migrationService.startMigration(new MigrationJob(applicationContext, databaseFile), migrationCallback());
            return null;
        };
    }

    private MigrationCallback migrationCallback() {
        return migrationStatus -> callbackHandler.post(() -> {
            // Pass back to main activity and notification dispatcher.
        });
    }

}
