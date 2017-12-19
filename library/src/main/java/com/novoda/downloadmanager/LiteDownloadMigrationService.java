package com.novoda.downloadmanager;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LiteDownloadMigrationService extends Service implements MigrationService {

    private static final String TAG = "MigrationService";
    private static ExecutorService executor;

    private IBinder binder;
    private MigrationServiceBinder.Callback migrationCallback;
    private NotificationCreator<MigrationStatus> notificationCreator;
    private NotificationChannelCreator notificationChannelCreator;
    private NotificationManager notificationManager;

    private void startMigration() {
        Optional<NotificationChannel> notificationChannel = notificationChannelCreator.createNotificationChannel();
        String channelName = notificationChannelCreator.getNotificationChannelName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationChannel.isPresent() && notificationChannelDoesNotExist(channelName)) {
            notificationManager.createNotificationChannel(notificationChannel.get());
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                Migrator migrator = MigrationFactory.createVersionOneToVersionTwoMigrator(
                        getApplicationContext(),
                        getDatabasePath("downloads.db"),
                        LiteDownloadMigrationService.this,
                        notificationChannelCreator,
                        notificationCreator
                );
                Log.d(TAG, "Begin Migration: " + migrator.getClass());
                migrator.migrate();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.O)
    private boolean notificationChannelDoesNotExist(String channelName) {
        return notificationManager.getNotificationChannel(channelName) == null;
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
        notificationChannelCreator = new MigrationNotificationChannelCreator(getResources());
        notificationCreator = new MigrationNotification(getApplicationContext(), android.R.drawable.ic_dialog_alert);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    class MigrationDownloadServiceBinder extends Binder {

        MigrationDownloadServiceBinder withUpdates(MigrationServiceBinder.Callback migrationCallback) {
            LiteDownloadMigrationService.this.migrationCallback = migrationCallback;
            return this;
        }

        MigrationDownloadServiceBinder withNotificationCreator(NotificationCreator<MigrationStatus> notificationCreator) {
            LiteDownloadMigrationService.this.notificationCreator = notificationCreator;
            return this;
        }

        MigrationDownloadServiceBinder withNotificationChannelCreator(NotificationChannelCreator notificationChannelCreator) {
            LiteDownloadMigrationService.this.notificationChannelCreator = notificationChannelCreator;
            return this;
        }

        void onDependenciesBound() {
            startMigration();
        }

    }

}
