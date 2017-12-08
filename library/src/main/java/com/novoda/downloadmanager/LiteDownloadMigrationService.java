package com.novoda.downloadmanager;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.novoda.notils.exception.DeveloperError;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LiteDownloadMigrationService extends Service {

    private Migrator v1ToV2Migrator;

    private ExecutorService executor;
    private IBinder binder;

    @Override
    public void onCreate() {
        executor = Executors.newSingleThreadExecutor();
        binder = new MigrationDownloadServiceBinder();

        super.onCreate();
    }

    private void migrateFromV1ToV2() {
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

    public class MigrationDownloadServiceBinder extends Binder {

        public MigrationDownloadServiceBinder withMigrator(Migrator migrator) {
            LiteDownloadMigrationService.this.v1ToV2Migrator = migrator;
            return this;
        }

        public void bind() {
            if (LiteDownloadMigrationService.this.v1ToV2Migrator == null) {
                throw new DeveloperError("You need to set a `Migrator` before calling `bind()`.");
            }
            LiteDownloadMigrationService.this.migrateFromV1ToV2();
        }
    }

}
