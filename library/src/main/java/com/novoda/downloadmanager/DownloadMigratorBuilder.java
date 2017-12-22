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

    private NotificationMetadata<MigrationStatus> notificationMetadata;

    public static DownloadMigratorBuilder newInstance(Context context) {
        Context applicationContext = context.getApplicationContext();

        String channelId = context.getResources().getString(R.string.migration_notification_channel_name);
        String channelDescription = context.getResources().getString(R.string.migration_notification_channel_description);
        NotificationCustomizer<MigrationStatus> customizer = new MigrationNotificationCustomizer();
        NotificationMetadata<MigrationStatus> defaultNotificationMetadata = new NotificationConfig<>(
                applicationContext,
                channelId,
                channelDescription,
                customizer,
                NotificationManagerCompat.IMPORTANCE_LOW
        );

        Handler handler = new Handler(Looper.getMainLooper());
        return new DownloadMigratorBuilder(applicationContext, handler, defaultNotificationMetadata);
    }

    private DownloadMigratorBuilder(Context applicationContext,
                                    Handler handler,
                                    NotificationMetadata<MigrationStatus> notificationMetadata) {
        this.applicationContext = applicationContext;
        this.handler = handler;
        this.notificationMetadata = notificationMetadata;
    }

    public DownloadMigratorBuilder withNotificationMetadata(NotificationMetadata<MigrationStatus> notificationMetadata) {
        this.notificationMetadata = notificationMetadata;
        return this;
    }

    public DownloadMigrator build() {
        return new LiteDownloadMigrator(applicationContext, handler, notificationMetadata);
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
