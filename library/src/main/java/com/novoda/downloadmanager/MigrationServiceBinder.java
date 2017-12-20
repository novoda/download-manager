package com.novoda.downloadmanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;

class MigrationServiceBinder implements DownloadMigrationService {

    private final Context applicationContext;

    MigrationServiceBinder(Context context) {
        this.applicationContext = context.getApplicationContext();
    }

    @Override
    public MigrationFuture startMigration(final NotificationChannelCreator notificationChannelCreator,
                                          final NotificationCreator<MigrationStatus> notificationCreator) {
        MigrationServiceConnection serviceConnection = new MigrationServiceConnection(notificationChannelCreator, notificationCreator);
        Intent serviceIntent = new Intent(applicationContext, LiteDownloadMigrationService.class);
        applicationContext.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        applicationContext.startService(serviceIntent);
        return serviceConnection;
    }

    class MigrationServiceConnection implements ServiceConnection, MigrationFuture {

        private final NotificationChannelCreator notificationChannelCreator;
        private final NotificationCreator<MigrationStatus> notificationCreator;
        private MigrationCallback migrationCallback = MigrationCallback.NO_OP;

        MigrationServiceConnection(NotificationChannelCreator notificationChannelCreator, NotificationCreator<MigrationStatus> notificationCreator) {
            this.notificationChannelCreator = notificationChannelCreator;
            this.notificationCreator = notificationCreator;
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            DownloadMigrationService migrationService = ((LiteDownloadMigrationService.MigrationDownloadServiceBinder) iBinder).getService();
            MigrationFuture migrationFuture = migrationService.startMigration(notificationChannelCreator, notificationCreator);
            migrationFuture.observe(migrationCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // Do nothing.
        }

        @Override
        public void observe(@NonNull MigrationCallback migrationCallback) {
            this.migrationCallback = migrationCallback;
        }
    }

}
