package com.novoda.downloadmanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class MigrationServiceBinder {

    private final Context context;

    private MigrationServiceConnection serviceConnection;

    public MigrationServiceBinder(Context context) {
        this.context = context;
    }

    public void bind() {
        if (serviceConnection == null) {
            serviceConnection = new MigrationServiceConnection();
        }

        Intent serviceIntent = new Intent(context, LiteDownloadMigrationService.class);
        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbind() {
        context.unbindService(serviceConnection);
        context.stopService(new Intent(context, LiteDownloadMigrationService.class));
        serviceConnection = null;
    }

    class MigrationServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ((LiteDownloadMigrationService.MigrationDownloadServiceBinder) iBinder)
                    .migrate();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // Do nothing.
        }
    }

}
