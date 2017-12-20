package com.novoda.downloadmanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class MigrationServiceBinder {

    private final Context applicationContext;
    private final Callback migrationCallback;
    private final NotificationChannelCreator notificationChannelCreator;
    private final NotificationCreator<MigrationStatus> notificationCreator;

    public interface Callback {
        void onUpdate(MigrationStatus migrationStatus);
    }

    MigrationServiceBinder(Context applicationContext,
                           Callback migrationCallback,
                           NotificationChannelCreator notificationChannelCreator,
                           NotificationCreator<MigrationStatus> notificationCreator) {
        this.applicationContext = applicationContext;
        this.migrationCallback = migrationCallback;
        this.notificationChannelCreator = notificationChannelCreator;
        this.notificationCreator = notificationCreator;
    }

    private MigrationServiceConnection serviceConnection;

    public void migrate() {
        if (serviceConnection != null) {
            return;
        }

        serviceConnection = new MigrationServiceConnection();
        Intent serviceIntent = new Intent(applicationContext, LiteDownloadMigrationService.class);
        applicationContext.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        applicationContext.startService(serviceIntent);
    }

    public void dispose() {
        if (serviceConnection != null) {
            applicationContext.unbindService(serviceConnection);
            applicationContext.stopService(new Intent(applicationContext, LiteDownloadMigrationService.class));
            serviceConnection = null;
        }
    }

    class MigrationServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            DownloadMigrationService migrationService = ((LiteDownloadMigrationService.MigrationDownloadServiceBinder) iBinder).getService();
            migrationService.setMigrationCallback(migrationCallback);
            migrationService.setNotificationChannelCreator(notificationChannelCreator);
            migrationService.setNotificationCreator(notificationCreator);
            migrationService.startMigration();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // Do nothing.
        }
    }

}
