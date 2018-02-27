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

    private int notificationId;
    private NotificationCompat.Builder builder;

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
                int newNotificationId = getId();

                if (builder == null || newNotificationId != notificationId) {
                    builder = new NotificationCompat.Builder(applicationContext, notificationChannelProvider.channelId());
                    notificationId = newNotificationId;
                }

                return notificationCustomizer.customNotificationFrom(builder, notificationPayload);
            }

            @Override
            public NotificationCustomizer.NotificationDisplayState notificationDisplayState() {
                return notificationCustomizer.notificationDisplayState(notificationPayload);
            }
        };
    }
}
