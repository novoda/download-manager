package com.novoda.downloadmanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;

class LiteDownloadMigrator implements DownloadMigrator {

    private final Context applicationContext;
    private final Handler handler;
    private final NotificationCreator<MigrationStatus> notificationCreator;

    LiteDownloadMigrator(Context context,
                         Handler handler,
                         NotificationCreator<MigrationStatus> notificationCreator) {
        this.applicationContext = context.getApplicationContext();
        this.handler = handler;
        this.notificationCreator = notificationCreator;
    }

    @Override
    public void startMigration(final MigrationCallback migrationCallback) {
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                DownloadMigrationService migrationService = ((LiteDownloadMigrationService.MigrationDownloadServiceBinder) binder).getService();
                migrationService.setNotificationCreator(notificationCreator);

                MigrationCallback mainThreadReportingMigrationCallback = new MigrationCallback() {
                    @Override
                    public void onUpdate(final MigrationStatus migrationStatus) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                migrationCallback.onUpdate(migrationStatus);
                            }
                        });
                    }
                };

                migrationService.startMigration(mainThreadReportingMigrationCallback);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                // do nothing.
            }
        };
        Intent serviceIntent = new Intent(applicationContext, LiteDownloadMigrationService.class);
        applicationContext.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        applicationContext.startService(serviceIntent);
    }

}
