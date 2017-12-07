package com.novoda.downloadmanager;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LiteDownloadMigrationService extends Service {

    private final Migrator.Callback deleteOldDatabaseOnCompletion = new Migrator.Callback() {
        @Override
        public void onMigrationComplete() {
            deleteDatabase("downloads.db");
        }
    };
    private final Migrator v1ToV2Migrator = MigrationFactory.createVersionOneToVersionTwoMigrator(
            getApplicationContext(),
            getDatabasePath("downloads.db"),
            deleteOldDatabaseOnCompletion
    );

    private ExecutorService executor;
    private IBinder binder;

    public class MigrationDownloadServiceBinder extends Binder {
        public LiteDownloadMigrationService getService() {
            return LiteDownloadMigrationService.this;
        }
    }

    @Override
    public void onCreate() {
        executor = Executors.newSingleThreadExecutor();
        binder = new MigrationDownloadServiceBinder();

        super.onCreate();
    }

    public void migrateFromV1ToV2() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                v1ToV2Migrator.migrate();
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }

}
