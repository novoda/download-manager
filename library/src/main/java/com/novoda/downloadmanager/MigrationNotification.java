package com.novoda.downloadmanager;

import android.app.Notification;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.v4.app.NotificationCompat;

class MigrationNotification implements NotificationCreator<MigrationStatus> {

    private static final int MAX_PROGRESS = 100;
    private final Context context;
    private final int iconDrawable;

    MigrationNotification(Context context, @DrawableRes int iconDrawable) {
        this.context = context.getApplicationContext();
        this.iconDrawable = iconDrawable;
    }

    @Override
    public NotificationInformation createNotification(String notificationChannelName, MigrationStatus migrationStatus) {
        String title = migrationStatus.status().toRawValue();
        String content = migrationStatus.percentageMigrated() + "% migrated";
        Notification notification = new NotificationCompat.Builder(context, notificationChannelName)
                .setProgress(MAX_PROGRESS, migrationStatus.percentageMigrated(), false)
                .setSmallIcon(iconDrawable)
                .setContentTitle(title)
                .setContentText(content)
                .build();

        return new MigrationNotificationInformation(migrationStatus.hashCode(), notification);
    }

    private static class MigrationNotificationInformation implements NotificationInformation {

        private final int id;
        private final Notification notification;

        MigrationNotificationInformation(int id, Notification notification) {
            this.id = id;
            this.notification = notification;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public Notification getNotification() {
            return notification;
        }
    }
}
