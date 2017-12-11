package com.novoda.downloadmanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LiteDownloadMigrationService extends Service {

    private Migrator v1ToV2Migrator;
    private ExecutorService executor;
    private IBinder binder;
    private Migrator.Callback migrationCallback;
    private String updateMessage;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.d(getClass().getSimpleName(), "onStartCommand");

        if (v1ToV2Migrator != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Log.d(getClass().getSimpleName(), "Begin Migration");
                    v1ToV2Migrator.migrate();
                }
            });
        }

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.d(getClass().getSimpleName(), "onCreate");
        executor = Executors.newSingleThreadExecutor();
        binder = new MigrationDownloadServiceBinder();
        v1ToV2Migrator = MigrationFactory.createVersionOneToVersionTwoMigrator(
                getApplicationContext(),
                getDatabasePath("downloads.db"),
                migrationCallback()
        );

        super.onCreate();
    }

    private Migrator.Callback migrationCallback() {
        return new Migrator.Callback() {
            @Override
            public void onUpdate(String message) {
                updateMessage = message;
                if (migrationCallback != null) {
                    migrationCallback.onUpdate(message);
                }
            }
        };
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(getClass().getSimpleName(), "onTaskRemoved");
        rescheduleMigration();
        Log.d(getClass().getSimpleName(), "Rescheduling");
        super.onTaskRemoved(rootIntent);
    }

    private void rescheduleMigration() {
        Intent intent = new Intent(getApplicationContext(), LiteDownloadMigrationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.w(getClass().getSimpleName(), "Could not retrieve AlarmManager for rescheduling.");
            return;
        }
        alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 5000, pendingIntent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(getClass().getSimpleName(), "onUnbind");
        migrationCallback = null;
        Log.d(getClass().getSimpleName(), "Stopping service");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(getClass().getSimpleName(), "onDestroy");
        executor.shutdown();
        super.onDestroy();
    }

    class MigrationDownloadServiceBinder extends Binder {

        MigrationDownloadServiceBinder withCallback(Migrator.Callback migrationCallback) {
            LiteDownloadMigrationService.this.migrationCallback = migrationCallback;
            return this;
        }

        void bind() {
            LiteDownloadMigrationService.this.migrationCallback.onUpdate(LiteDownloadMigrationService.this.updateMessage);
        }

    }

}
