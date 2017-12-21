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

import static com.novoda.downloadmanager.MigrationStatus.Status;

public class LiteDownloadMigrationService extends Service implements DownloadMigrationService {

    private static final String TAG = "MigrationService";
    private static ExecutorService executor;

    private IBinder binder;
    private NotificationChannelCreator notificationChannelCreator;
    private NotificationCreator<MigrationStatus> notificationCreator;
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();
        }

        binder = new MigrationDownloadServiceBinder();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        super.onCreate();
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

    @Override
    public void setNotificationChannelCreator(NotificationChannelCreator notificationChannelCreator) {
        this.notificationChannelCreator = notificationChannelCreator;
    }

    @Override
    public void setNotificationCreator(NotificationCreator<MigrationStatus> notificationCreator) {
        this.notificationCreator = notificationCreator;
    }

    @Override
    public void startMigration(MigrationCallback migrationCallback) {
        createNotificationChannel();
        MigrationJob migrationJob = new MigrationJob(getApplicationContext(), getDatabasePath("downloads.db"));
        migrationJob.addCallback(migrationCallback);
        migrationJob.addCallback(notificationMigrationCallback);
        executor.execute(migrationJob);
    }

    private void createNotificationChannel() {
        Optional<NotificationChannel> notificationChannel = notificationChannelCreator.createNotificationChannel();
        String channelName = notificationChannelCreator.getNotificationChannelId();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationChannel.isPresent() && notificationChannelDoesNotExist(channelName)) {
            notificationManager.createNotificationChannel(notificationChannel.get());
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private boolean notificationChannelDoesNotExist(String channelName) {
        return notificationManager.getNotificationChannel(channelName) == null;
    }

    private final MigrationCallback notificationMigrationCallback = new MigrationCallback() {
        @Override
        public void onUpdate(MigrationStatus migrationStatus) {
            String channelName = notificationChannelCreator.getNotificationChannelId();
            NotificationInformation notification = notificationCreator.createNotification(channelName, migrationStatus); // this is NotificationConfig

            if (migrationStatus.status() == Status.COMPLETE || migrationStatus.status() == Status.DB_NOT_PRESENT) {
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
