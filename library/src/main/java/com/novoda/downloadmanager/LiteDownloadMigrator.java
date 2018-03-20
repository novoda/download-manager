package com.novoda.downloadmanager;

import android.content.Context;
import android.os.Handler;

import java.io.File;
import java.util.concurrent.ExecutorService;

class LiteDownloadMigrator implements DownloadMigrator {

    private final Object waitForMigrationService;
    private final ExecutorService executor;
    private final Handler callbackHandler;
    private final MigrationCallback migrationCallback;
    private final ServiceNotificationDispatcher<MigrationStatus> notificationDispatcher;

    private DownloadMigrationService migrationService;

    private final Context applicationContext;

    LiteDownloadMigrator(Context context,
                         Object waitForMigrationService,
                         ExecutorService executor,
                         Handler callbackHandler,
                         MigrationCallback migrationCallback,
                         ServiceNotificationDispatcher<MigrationStatus> notificationDispatcher) {
        this.applicationContext = context.getApplicationContext();
        this.waitForMigrationService = waitForMigrationService;
        this.executor = executor;
        this.callbackHandler = callbackHandler;
        this.migrationCallback = migrationCallback;
        this.notificationDispatcher = notificationDispatcher;
    }

    void initialise(DownloadMigrationService migrationService) {
        this.migrationService = migrationService;
        notificationDispatcher.setService(migrationService);

        synchronized (waitForMigrationService) {
            waitForMigrationService.notifyAll();
        }
    }

    @Override
    public void startMigration(String jobIdentifier, File databaseFile, String basePath) {
        executor.submit(() -> Wait.<Void>waitFor(migrationService, waitForMigrationService)
                .thenPerform(executeMigrationFor(jobIdentifier, databaseFile, basePath)));
    }

    private Wait.ThenPerform.Action<Void> executeMigrationFor(String jobIdentifier, File databaseFile, String basePath) {
        return () -> {
            migrationService.startMigration(new MigrationJob(applicationContext, jobIdentifier, databaseFile, basePath), migrationCallback());
            return null;
        };
    }

    private MigrationCallback migrationCallback() {
        return migrationStatus -> callbackHandler.post(() -> {
            migrationCallback.onUpdate(migrationStatus);
            notificationDispatcher.updateNotification(migrationStatus);
        });
    }

}
