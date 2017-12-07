package com.novoda.downloadmanager;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TODO: You will need a new one of these to perform the migration. Would suggest again to take
// inspiration from Merlin to not rely on `null` checks but rather use an `isBound` method.
// https://github.com/novoda/merlin/blob/master/core/src/main/java/com/novoda/merlin/service/MerlinService.java.
// Services are not run in their own threads by default so we need to handle that too.
public class LiteDownloadMigrationService extends Service {

    private static final String WAKELOCK_TAG = "WakelockTag";
    private static final String NOTIFICATION_TAG = "download-manager";

    private ExecutorService executor;
    private IBinder binder;
    private PowerManager.WakeLock wakeLock;
    private NotificationManagerCompat notificationManagerCompat;

    private final Migrator.Callback completionCallback = new Migrator.Callback() {
        @Override
        public void onMigrationComplete() {
            deleteDatabase("downloads.db");
        }
    };

    public class MigrationDownloadServiceBinder extends Binder {
        public LiteDownloadMigrationService getService() {
            return LiteDownloadMigrationService.this;
        }
    }

    @Override
    public void onCreate() {
        executor = Executors.newSingleThreadExecutor();
        binder = new MigrationDownloadServiceBinder();
        notificationManagerCompat = NotificationManagerCompat.from(this);

        super.onCreate();
    }

    public void migrate() {
        showNotification();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                Migrator migrator = MigrationFactory.createVersionOneToVersionTwoMigrator(
                        getApplicationContext(),
                        getDatabasePath("downloads.db"),
                        completionCallback
                );
                migrator.migrate();
            }
        });
    }

    private void showNotification() {
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), "migration-channel")
                .setContentText("Migrating downloads")
                .build();
        startForeground(101, notification);
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

    private void updateNotification(NotificationInformation notificationInformation) {
        startForeground(notificationInformation.getId(), notificationInformation.getNotification());
    }

    private void stackNotification(NotificationInformation notificationInformation) {
        stopForeground(true);
        showFinalDownloadedNotification(notificationInformation);
    }

    private void showFinalDownloadedNotification(NotificationInformation notificationInformation) {
        Notification notification = notificationInformation.getNotification();
        notificationManagerCompat.notify(NOTIFICATION_TAG, notificationInformation.getId(), notification);
    }

    private void dismissNotification() {
        stopForeground(true);
    }

    private void acquireCpuWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG);
            wakeLock.acquire();
        }
    }

    private void releaseCpuWakeLock() {
        wakeLock.release();
    }

    @Override
    public void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }

}
