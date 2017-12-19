package com.novoda.downloadmanager;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LiteDownloadMigrationService extends Service implements MigrationService {

    private static final String TAG = "MigrationService";
    private static ExecutorService executor;

    private IBinder binder;
    private Migrator.Callback migrationCallback;
    private NotificationCreator<MigrationStatus> notificationCreator;
    private NotificationChannelCreator channelCreator;
    private NotificationManager notificationManager;
    private MigrationStatus migrationStatus;

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
                        LiteDownloadMigrationService.this,
                        channelCreator,
                        notificationCreator
                );
                Log.d(TAG, "Begin Migration: " + migrator.getClass());
                migrator.migrate();
            }
        });

        return START_STICKY;
    }

    @Override
    public void updateNotification(NotificationInformation notificationInformation) {
        startForeground(notificationInformation.getId(), notificationInformation.getNotification());
    }

    @Override
    public void stackNotification(NotificationInformation notificationInformation) {
        stopForeground(true);
        Notification notification = notificationInformation.getNotification();
        notificationManager.notify(notificationInformation.getId(), notification);
    }

    @Override
    public void updateMessage(MigrationStatus migrationStatus) {
        if (migrationCallback != null) {
            migrationCallback.onUpdate(migrationStatus);
        }
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();
        }

        binder = new MigrationDownloadServiceBinder();
        channelCreator = new MigrationNotificationChannelCreator(getResources());
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Optional<NotificationChannel> notificationChannel = channelCreator.createNotificationChannel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationChannel.isPresent()) {
            notificationManager.createNotificationChannel(notificationChannel.get());
        }

        notificationCreator = new MigrationNotification(getApplicationContext(), android.R.drawable.ic_dialog_alert);

        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
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
        return super.onUnbind(intent);
    }

    class MigrationDownloadServiceBinder extends Binder {

        MigrationDownloadServiceBinder withCallback(Migrator.Callback migrationCallback) {
            LiteDownloadMigrationService.this.migrationCallback = migrationCallback;
            return this;
        }

        void bind() {
            if (migrationStatus != null && migrationCallback != null) {
                LiteDownloadMigrationService.this.migrationCallback.onUpdate(migrationStatus);
            }
        }

    }

}
