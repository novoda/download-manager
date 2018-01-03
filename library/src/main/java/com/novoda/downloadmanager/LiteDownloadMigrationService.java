package com.novoda.downloadmanager;

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
    private static volatile ExecutorService singleInstanceExecutor = Executors.newSingleThreadExecutor();

    private IBinder binder;
    private NotificationManager notificationManager;
    private NotificationCreator<MigrationStatus> notificationCreator;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
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
    public void setNotificationCreator(NotificationCreator<MigrationStatus> notificationCreator) {
        this.notificationCreator = notificationCreator;
    }

    @Override
    public void startMigration(MigrationCallback migrationCallback) {
        createNotificationChannel();
        MigrationJob migrationJob = new MigrationJob(getApplicationContext(), getDatabasePath("downloads.db"));
        migrationJob.addCallback(migrationCallback);
        migrationJob.addCallback(notificationMigrationCallback);
        singleInstanceExecutor.execute(migrationJob);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = notificationCreator.createNotificationChannel();
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private final MigrationCallback notificationMigrationCallback = new MigrationCallback() {
        @Override
        public void onUpdate(MigrationStatus migrationStatus) {
            NotificationInformation notification = notificationCreator.createNotification(migrationStatus);

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
