package com.novoda.downloadmanager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

public class DownloadMigratorBuilder {

    private final Context applicationContext;
    private final Handler handler;
    private NotificationChannelCreator notificationChannelCreator;
    private NotificationCreator<MigrationStatus> notificationCreator;

    public static DownloadMigratorBuilder newInstance(Context context) {
        Context applicationContext = context.getApplicationContext();
        NotificationChannelCreator notificationChannelCreator = new MigrationNotificationChannelCreator(applicationContext.getResources());
        NotificationCreator<MigrationStatus> notificationCreator = new MigrationNotification(applicationContext, android.R.drawable.ic_menu_gallery);
        Handler handler = new Handler(Looper.getMainLooper());
        return new DownloadMigratorBuilder(applicationContext, handler, notificationChannelCreator, notificationCreator);
    }

    private DownloadMigratorBuilder(Context applicationContext,
                                    Handler handler,
                                    NotificationChannelCreator notificationChannelCreator,
                                    NotificationCreator<MigrationStatus> notificationCreator) {
        this.applicationContext = applicationContext;
        this.handler = handler;
        this.notificationChannelCreator = notificationChannelCreator;
        this.notificationCreator = notificationCreator;
    }

    public DownloadMigratorBuilder withNotificationChannelCreator(NotificationChannelCreator notificationChannelCreator) {
        this.notificationChannelCreator = notificationChannelCreator;
        return this;
    }

    public DownloadMigratorBuilder withNotificationCreator(NotificationCreator<MigrationStatus> notificationCreator) {
        this.notificationCreator = notificationCreator;
        return this;
    }

    public DownloadMigrator build() {
        return new DownloadMigrator(applicationContext, handler, notificationChannelCreator, notificationCreator);
    }
}
