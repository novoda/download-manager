package com.novoda.downloadmanager;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

class MigrationStatusNotificationCreator implements NotificationCreator<MigrationStatus> {

    private final Context applicationContext;
    private final NotificationCustomizer<MigrationStatus> notificationCustomizer;
    private NotificationChannelProvider notificationChannelProvider;

    MigrationStatusNotificationCreator(Context context,
                                       NotificationCustomizer<MigrationStatus> customizer,
                                       NotificationChannelProvider notificationChannelProvider) {
        this.applicationContext = context.getApplicationContext();
        this.notificationCustomizer = customizer;
        this.notificationChannelProvider = notificationChannelProvider;
    }

    @Override
    public void setNotificationChannelProvider(NotificationChannelProvider notificationChannelProvider) {
        this.notificationChannelProvider = notificationChannelProvider;
    }

    private MigrationStatus.Status previousStatus;
    private NotificationCompat.Builder builder;

    @Override
    public NotificationInformation createNotification(final MigrationStatus migrationStatus) {
        return new NotificationInformation() {
            @Override
            public int getId() {
                return migrationStatus.migrationId().hashCode();
            }

            @Override
            public Notification getNotification() {
                MigrationStatus.Status status = migrationStatus.status();

                if (builder == null || !previousStatus.equals(status)) {
                    builder = new NotificationCompat.Builder(applicationContext, notificationChannelProvider.channelId());
                    previousStatus = status;
                }

                return notificationCustomizer.customNotificationFrom(builder, migrationStatus);
            }

            @Override
            public NotificationCustomizer.NotificationDisplayState notificationDisplayState() {
                return notificationCustomizer.notificationDisplayState(migrationStatus);
            }
        };
    }
}
