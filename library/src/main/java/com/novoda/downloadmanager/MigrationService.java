package com.novoda.downloadmanager;

import android.content.Context;

public class MigrationService implements ManagedDownloadMigrationService {

    private final Context applicationContext;

    public MigrationService(Context context) {
        this.applicationContext = context.getApplicationContext();
    }

    @Override
    public MigrationFuture startMigration(NotificationChannelCreator notificationChannelCreator, NotificationCreator<MigrationStatus> notificationCreator) {
        return new MigrationServiceBinder(applicationContext).startMigration(notificationChannelCreator, notificationCreator);
    }

    @Override
    public MigrationFuture startMigration() {
        NotificationChannelCreator notificationChannelCreator = new MigrationNotificationChannelCreator(applicationContext.getResources());
        NotificationCreator<MigrationStatus> notificationCreator = new MigrationNotification(applicationContext, android.R.drawable.ic_dialog_alert);
        return startMigration(notificationChannelCreator, notificationCreator);
    }
}
