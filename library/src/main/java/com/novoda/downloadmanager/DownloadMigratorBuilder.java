package com.novoda.downloadmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

public final class DownloadMigratorBuilder {

    private final Context applicationContext;
    private final Handler handler;

    private NotificationChannelProvider notificationChannelProvider;
    private NotificationCreator<MigrationStatus> notificationCreator;

    public static DownloadMigratorBuilder newInstance(Context context) {
        Context applicationContext = context.getApplicationContext();
        Resources resources = context.getResources();

        DefaultNotificationChannelProvider notificationChannelProvider = new DefaultNotificationChannelProvider(
                resources.getString(R.string.download_notification_channel_name),
                resources.getString(R.string.download_notification_channel_description),
                NotificationManagerCompat.IMPORTANCE_LOW
        );
        NotificationCustomizer<MigrationStatus> customizer = new MigrationNotificationCustomizer(context.getResources());
        NotificationCreator<MigrationStatus> defaultNotificationCreator = new NotificationCreator<>(
                applicationContext,
                customizer,
                notificationChannelProvider
        );

        Handler handler = new Handler(Looper.getMainLooper());
        return new DownloadMigratorBuilder(applicationContext, handler, notificationChannelProvider, defaultNotificationCreator);
    }

    private DownloadMigratorBuilder(Context applicationContext,
                                    Handler handler,
                                    NotificationChannelProvider notificationChannelProvider,
                                    NotificationCreator<MigrationStatus> notificationCreator) {
        this.applicationContext = applicationContext;
        this.handler = handler;
        this.notificationChannelProvider = notificationChannelProvider;
        this.notificationCreator = notificationCreator;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public DownloadMigratorBuilder withNotificationChannel(NotificationChannel notificationChannel) {
        this.notificationChannelProvider = new OreoNotificationChannelProvider(notificationChannel);
        this.notificationCreator.setNotificationChannelProvider(notificationChannelProvider);
        return this;
    }

    public DownloadMigratorBuilder withNotificationChannel(String channelId, String name, @Importance int importance) {
        this.notificationChannelProvider = new DefaultNotificationChannelProvider(channelId, name, importance);
        this.notificationCreator.setNotificationChannelProvider(notificationChannelProvider);
        return this;
    }

    public DownloadMigratorBuilder withNotification(NotificationCustomizer<MigrationStatus> notificationCustomizer) {
        this.notificationCreator = new NotificationCreator<>(applicationContext, notificationCustomizer, notificationChannelProvider);
        return this;
    }

    public DownloadMigrator build() {
        return new LiteDownloadMigrator(applicationContext, handler, notificationChannelProvider, notificationCreator);
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
