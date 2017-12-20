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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LiteDownloadMigrationService extends Service implements DownloadMigrationService {

    private static final String TAG = "MigrationService";
    private static ExecutorService executor;

    private IBinder binder;
    private NotificationCreator<MigrationStatus> notificationCreator;
    private NotificationChannelCreator notificationChannelCreator;
    private NotificationManager notificationManager;

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

    @Override
    public MigrationFuture startMigration(final NotificationChannelCreator notificationChannelCreator, NotificationCreator<MigrationStatus> notificationCreator) {
        return new MigrationFuture() {
            @Override
            public void observe(@NonNull final MigrationCallback migrationCallback) {
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
                                migrationStatusCallback(migrationCallback)
                        );
                        Log.d(TAG, "Begin Migration: " + migrator.getClass());
                        migrator.migrate();
                    }
                });
            }
        };
    }

    class MigrationDownloadServiceBinder extends Binder {
        DownloadMigrationService getService() {
            return LiteDownloadMigrationService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private boolean notificationChannelDoesNotExist(String channelName) {
        return notificationManager.getNotificationChannel(channelName) == null;
    }

    private Migrator.Callback migrationStatusCallback(final MigrationCallback migrationCallback) {
        return new Migrator.Callback() {
            @Override
            public void onUpdate(MigrationStatus migrationStatus) {
                String channelName = notificationChannelCreator.getNotificationChannelName();
                NotificationInformation notification = notificationCreator.createNotification(channelName, migrationStatus);

                migrationCallback.onUpdate(migrationStatus);

                if (migrationStatus.status() == MigrationStatus.Status.COMPLETE) {
                    stackNotification(notification);
                } else {
                    updateNotification(notification);
                }
            }

            private void updateNotification(NotificationInformation notificationInformation) {
                startForeground(notificationInformation.getId(), notificationInformation.getNotification());
            }

            private void stackNotification(NotificationInformation notificationInformation) {
                stopForeground(true);
                Notification notification = notificationInformation.getNotification();
                notificationManager.notify(notificationInformation.getId(), notification);
            }
        };
    }

}
