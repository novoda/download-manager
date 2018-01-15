package com.novoda.downloadmanager;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DOWNLOADED;

public class LiteDownloadService extends Service implements DownloadService {

    private static final long TEN_MINUTES_IN_MILLIS = TimeUnit.MINUTES.toMillis(10);
    private static final String WAKELOCK_TAG = "WakelockTag";
    private static final String NOTIFICATION_TAG = "download-manager";

    private ExecutorService executor;
    private IBinder binder;
    private PowerManager.WakeLock wakeLock;
    private NotificationManagerCompat notificationManagerCompat;

    @Override
    public void onCreate() {
        super.onCreate();
        executor = Executors.newSingleThreadExecutor();
        binder = new DownloadServiceBinder();
        notificationManagerCompat = NotificationManagerCompat.from(this);
    }

    class DownloadServiceBinder extends Binder {
        DownloadService getService() {
            return LiteDownloadService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void download(final DownloadBatch downloadBatch, final DownloadBatchCallback callback) {
        downloadBatch.setCallback(callback);

        if (downloadBatch.status().status() == DOWNLOADED) {
            return;
        }

        callback.onUpdate(downloadBatch.status());

        executor.execute(() -> {
            acquireCpuWakeLock();
            downloadBatch.download();
            releaseCpuWakeLock();
        });
    }

    @Override
    public void updateNotification(NotificationInformation notificationInformation) {
        startForeground(notificationInformation.getId(), notificationInformation.getNotification());
    }

    @Override
    public void stackNotification(NotificationInformation notificationInformation) {
        stopForeground(true);
        showFinalDownloadedNotification(notificationInformation);
    }

    private void showFinalDownloadedNotification(NotificationInformation notificationInformation) {
        Notification notification = notificationInformation.getNotification();
        notificationManagerCompat.notify(NOTIFICATION_TAG, notificationInformation.getId(), notification);
    }

    @Override
    public void dismissNotification() {
        stopForeground(true);
    }

    private void acquireCpuWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG);
            wakeLock.acquire(TEN_MINUTES_IN_MILLIS);
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

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }
}
