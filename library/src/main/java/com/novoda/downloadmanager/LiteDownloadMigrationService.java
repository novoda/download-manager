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

    private static final String TAG = "MigrationService";
    private static ExecutorService executor;

    private IBinder binder;
    private Migrator.Callback migrationCallback;
    private String updateMessage;

    private static boolean isBound;

    public static boolean isBound() {
        return isBound;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand");

        executor.execute(new Runnable() {
            @Override
            public void run() {
                Migrator migrator = MigrationFactory.createVersionOneToVersionTwoMigrator(
                        getApplicationContext(),
                        getDatabasePath("downloads.db"),
                        migrationCallback()
                );
                Log.d(TAG, "Begin Migration: " + migrator.getClass());
                migrator.migrate();
            }
        });

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();
        }

        binder = new MigrationDownloadServiceBinder();

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
        isBound = true;
        return binder;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "onTaskRemoved");
        rescheduleMigration();
        Log.d(TAG, "Rescheduling");
        super.onTaskRemoved(rootIntent);
    }

    private void rescheduleMigration() {
        Intent intent = new Intent(getApplicationContext(), LiteDownloadMigrationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.w(TAG, "Could not retrieve AlarmManager for rescheduling.");
            return;
        }
        alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 5000, pendingIntent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        migrationCallback = null;
        Log.d(TAG, "Stopping service");
        isBound = false;
        return super.onUnbind(intent);
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
