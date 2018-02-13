package com.novoda.downloadmanager;

import android.app.Notification;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

public final class DownloadMigratorBuilder {

    private final Context applicationContext;
    private final Handler handler;

    private NotificationCreator<MigrationStatus> notificationCreator;

    public static DownloadMigratorBuilder newInstance(Context context) {
        Context applicationContext = context.getApplicationContext();
        Resources resources = context.getResources();

        String channelId = resources.getString(R.string.download_notification_channel_name);
        String channelDescription = resources.getString(R.string.download_notification_channel_description);
        NotificationCustomizer<MigrationStatus> customizer = new MigrationNotificationCustomizer(context.getResources());
        NotificationCreator<MigrationStatus> defaultNotificationCreator = NotificationCreator.create(
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

        private final Resources resources;

        MigrationNotificationCustomizer(Resources resources) {
            this.resources = resources;
        }

        @Override
        public Notification customNotificationFrom(NotificationCompat.Builder builder, MigrationStatus payload) {
            String title = payload.status().toRawValue();
            String content = resources.getString(R.string.migration_notification_content_progress, payload.percentageMigrated());
            return builder
                    .setProgress(MAX_PROGRESS, payload.percentageMigrated(), false)
                    .setSmallIcon(android.R.drawable.ic_menu_gallery)
                    .setContentTitle(title)
                    .setContentText(content)
                    .build();
        }
    }
}
