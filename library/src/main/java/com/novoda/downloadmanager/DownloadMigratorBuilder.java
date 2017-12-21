package com.novoda.downloadmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;

public class DownloadMigratorBuilder {

    private final Context applicationContext;
    private final Handler handler;

    private NotificationConfig<MigrationStatus> notificationConfig;

    public static DownloadMigratorBuilder newInstance(Context context) {
        Context applicationContext = context.getApplicationContext();

        String channelId = context.getResources().getString(R.string.migration_notification_channel_name);
        String channelDescription = context.getResources().getString(R.string.migration_notification_channel_description);
        NotificationCustomiser<MigrationStatus> customiser = new NotificationCustomiser<MigrationStatus>() {
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

            @Override
            public int notificationId(MigrationStatus payload) {
                return payload.hashCode();
            }
        };
        NotificationConfig<MigrationStatus> config = new NotificationConfig<>(applicationContext, channelId, channelDescription, customiser, NotificationManager.IMPORTANCE_LOW);

        Handler handler = new Handler(Looper.getMainLooper());
        return new DownloadMigratorBuilder(applicationContext, handler, config);
    }

    private DownloadMigratorBuilder(Context applicationContext,
                                    Handler handler,
                                    NotificationConfig<MigrationStatus> notificationConfig) {
        this.applicationContext = applicationContext;
        this.handler = handler;
        this.notificationConfig = notificationConfig;
    }

    public DownloadMigratorBuilder withNotificationConfig(NotificationConfig<MigrationStatus> notificationConfig) {
        this.notificationConfig = notificationConfig;
        return this;
    }

    public DownloadMigrator build() {
        return new LiteDownloadMigrator(applicationContext, handler, notificationConfig, notificationConfig);
    }
}
