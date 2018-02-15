package com.novoda.downloadmanager;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

class NotificationCreator<T> {

    private final Context applicationContext;
    private final NotificationCustomizer<T> notificationCustomizer;
    private NotificationChannelProvider notificationChannelProvider;

    NotificationCreator(Context context, NotificationCustomizer<T> customizer, NotificationChannelProvider notificationChannelProvider) {
        this.applicationContext = context.getApplicationContext();
        this.notificationCustomizer = customizer;
        this.notificationChannelProvider = notificationChannelProvider;
    }

    void setNotificationChannelProvider(NotificationChannelProvider notificationChannelProvider) {
        this.notificationChannelProvider = notificationChannelProvider;
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
                NotificationCompat.Builder builder = new NotificationCompat.Builder(applicationContext, notificationChannelProvider.channelId());
                return notificationCustomizer.customNotificationFrom(builder, notificationPayload);
            }

            @Override
            public NotificationCustomizer.NotificationStackState notificationStackState() {
                return notificationCustomizer.notificationStackState(notificationPayload);
            }
        };
    }
}
