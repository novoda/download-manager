package com.novoda.downloadmanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class MigrationServiceBinder {

    private final Context context;
    private final Migrator.Callback migrationCallback;

    private MigrationServiceConnection serviceConnection;

    MigrationServiceBinder(Context context, Migrator.Callback migrationCallback) {
        this.context = context;
        this.migrationCallback = migrationCallback;
    }

    public void bind() {
        if (serviceConnection == null) {
            serviceConnection = new MigrationServiceConnection();
        }

        Intent serviceIntent = new Intent(context, LiteDownloadMigrationService.class);
        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        context.startService(new Intent(context, LiteDownloadMigrationService.class));
    }

    public void unbind() {
        if (LiteDownloadMigrationService.isBound() && migrationCallback != null) {
            context.unbindService(serviceConnection);
            context.stopService(new Intent(context, LiteDownloadMigrationService.class));
            serviceConnection = null;
        }
    }

    class MigrationServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ((LiteDownloadMigrationService.MigrationDownloadServiceBinder) iBinder)
                    .withCallback(migrationCallback)
                    .bind();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // Do nothing.
        }
    }

}
