package com.novoda.downloadmanager;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

class NotificationCreator<T> {

    private final Context applicationContext;
    private final String channelId;
    private final NotificationCustomizer<T> notificationCustomizer;

    NotificationCreator(Context context, String channelId, NotificationCustomizer<T> customizer) {
        this.applicationContext = context.getApplicationContext();
        this.channelId = channelId;
        this.notificationCustomizer = customizer;
    }

    NotificationInformation createNotification(final T notificationPayload) {
        return new NotificationInformation() {
            @Override
            public int getId() {
                if (notificationPayload instanceof DownloadBatchStatus) {
                    return ((DownloadBatchStatus) notificationPayload).getDownloadBatchId().hashCode();
                }

                return notificationPayload.hashCode();
            }

            @Override
            public Notification getNotification() {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(applicationContext, channelId);
                return notificationCustomizer.customNotificationFrom(builder, notificationPayload);
            }
        };
    }

    String channelId() {
        return channelId;
    }
}
