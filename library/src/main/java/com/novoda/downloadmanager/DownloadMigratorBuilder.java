package com.novoda.downloadmanager;

import android.app.Notification;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

public class DownloadMigratorBuilder {

    private final Context applicationContext;
    private final Handler handler;

    private NotificationCreator<MigrationStatus> notificationCreator;

    public static DownloadMigratorBuilder newInstance(Context context) {
        Context applicationContext = context.getApplicationContext();

        String channelId = context.getResources().getString(R.string.migration_notification_channel_name);
        String channelDescription = context.getResources().getString(R.string.migration_notification_channel_description);
        NotificationCustomizer<MigrationStatus> customizer = new MigrationNotificationCustomizer();
        NotificationCreator<MigrationStatus> defaultNotificationCreator = new NotificationCreator<>(
                applicationContext,
                channelId,
                channelDescription,
                NotificationManagerCompat.IMPORTANCE_LOW,
                customizer
        );

        Handler handler = new Handler(Looper.getMainLooper());
        return new DownloadMigratorBuilder(applicationContext, handler, defaultNotificationCreator);
    }

    private DownloadMigratorBuilder(Context applicationContext,
                                    Handler handler,
                                    NotificationCreator<MigrationStatus> notificationCreator) {
        this.applicationContext = applicationContext;
        this.handler = handler;
        this.notificationCreator = notificationCreator;
    }

    public DownloadMigratorBuilder withNotification(NotificationCreator<MigrationStatus> notificationCreator) {
        this.notificationCreator = notificationCreator;
        return this;
    }

    public DownloadMigrator build() {
        return new LiteDownloadMigrator(applicationContext, handler, notificationCreator);
    }

    private static class MigrationNotificationCustomizer implements NotificationCustomizer<MigrationStatus> {
        private static final int MAX_PROGRESS = 100;

        @Override
        public Notification customNotificationFrom(NotificationCompat.Builder builder, MigrationStatus payload) {
            String title = payload.status().toRawValue();
            String content = payload.percentageMigrated() + "% migrated";
            return builder
                    .setProgress(MAX_PROGRESS, payload.percentageMigrated(), false)
                    .setSmallIcon(android.R.drawable.ic_menu_gallery)
                    .setContentTitle(title)
                    .setContentText(content)
                    .build();
        }
    }
}
