package com.novoda.downloadmanager;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LiteDownloadMigrationService extends Service implements DownloadMigrationService {

    private static final long TEN_MINUTES_IN_MILLIS = TimeUnit.MINUTES.toMillis(10);
    private static final String WAKELOCK_TAG = "MigrationWakelockTag";

    private ExecutorService executor;
    private IBinder binder;
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        executor = Executors.newSingleThreadExecutor();
        binder = new MigrationDownloadServiceBinder();
        super.onCreate();
    }

    @Override
    public void start(int id, Notification notification) {
        startForeground(id, notification);
    }

    @Override
    public void stop(boolean removeNotification) {
        stopForeground(removeNotification);
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
    public void startMigration(MigrationJob migrationJob, MigrationCallback migrationCallback) {
        migrationJob.addCallback(migrationCallback);

        executor.execute(() -> {
            acquireCpuWakeLock();
            migrationJob.run();
            releaseHeldCpuWakeLock();
        });
    }

    private void acquireCpuWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG);
            wakeLock.acquire(TEN_MINUTES_IN_MILLIS);
        }
    }

    private void releaseHeldCpuWakeLock() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @Override
    public void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}
