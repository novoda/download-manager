package com.novoda.downloadmanager;

import android.app.Notification;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.v4.app.NotificationCompat;

class MigrationNotification implements NotificationCreator<MigrationStatus> {

    private final Context context;
    private final int iconDrawable;

    MigrationNotification(Context context, @DrawableRes int iconDrawable) {
        this.context = context.getApplicationContext();
        this.iconDrawable = iconDrawable;
    }

    @Override
    public NotificationInformation createNotification(String notificationChannelName, MigrationStatus migrationStatus) {
        String title = migrationStatus.message();

        Notification notification = new NotificationCompat.Builder(context, notificationChannelName)
                .setSmallIcon(iconDrawable)
                .setContentTitle(migrationStatus.message())
                .build();

        return new MigrationNotificationInformation(title.hashCode(), notification);
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
