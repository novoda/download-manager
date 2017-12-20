package com.novoda.downloadmanager;

import android.content.Context;
import android.os.Handler;

public class MigrationServiceBinderBuilder {

    private final Context applicationContext;
    private final Handler callbackHandler;
    private MigrationServiceBinder.Callback migrationCallback;
    private NotificationChannelCreator notificationChannelCreator;
    private NotificationCreator<MigrationStatus> notificationCreator;

    public static MigrationServiceBinderBuilder newInstance(Context context, Handler callbackHandler) {
        Context applicationContext = context.getApplicationContext();
        MigrationServiceBinder.Callback callback = new MigrationServiceBinder.Callback() {
            @Override
            public void onUpdate(MigrationStatus migrationStatus) {
                // do nothing.
            }
        };

        NotificationChannelCreator notificationChannelCreator = new MigrationNotificationChannelCreator(applicationContext.getResources());
        NotificationCreator<MigrationStatus> notificationCreator = new MigrationNotification(applicationContext, android.R.drawable.ic_dialog_alert);
        return new MigrationServiceBinderBuilder(applicationContext, callbackHandler, callback, notificationChannelCreator, notificationCreator);
    }

    private MigrationServiceBinderBuilder(Context applicationContext,
                                          Handler callbackHandler,
                                          MigrationServiceBinder.Callback migrationCallback,
                                          NotificationChannelCreator notificationChannelCreator,
                                          NotificationCreator<MigrationStatus> notificationCreator) {
        this.applicationContext = applicationContext;
        this.callbackHandler = callbackHandler;
        this.migrationCallback = migrationCallback;
        this.notificationChannelCreator = notificationChannelCreator;
        this.notificationCreator = notificationCreator;
    }

    public MigrationServiceBinderBuilder withMigrationUpdates(MigrationServiceBinder.Callback migrationCallback) {
        this.migrationCallback = migrationCallback;
        return this;
    }

    public MigrationServiceBinderBuilder withNotificationChannelCreator(NotificationChannelCreator notificationChannelCreator) {
        this.notificationChannelCreator = notificationChannelCreator;
        return this;
    }

    public MigrationServiceBinderBuilder withNotificationCreator(NotificationCreator<MigrationStatus> notificationCreator) {
        this.notificationCreator = notificationCreator;
        return this;
    }

    public MigrationServiceBinder build() {
        MigrationServiceBinder.Callback handlerReportingMigrationCallback = new MigrationServiceBinder.Callback() {
            @Override
            public void onUpdate(final MigrationStatus migrationStatus) {
                callbackHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        migrationCallback.onUpdate(migrationStatus);
                    }
                });
            }
        };

        return new MigrationServiceBinder(applicationContext, handlerReportingMigrationCallback, notificationChannelCreator, notificationCreator);
    }
}
