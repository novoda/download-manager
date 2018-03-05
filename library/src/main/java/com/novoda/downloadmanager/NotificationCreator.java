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

    private int previousNotificationId;
    private NotificationCustomizer.NotificationDisplayState previousNotificationDisplayState;
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
                int notificationId = getId();
                NotificationCustomizer.NotificationDisplayState notificationDisplayState = notificationDisplayState();

                if (builder == null || hasChanged(previousNotificationId, notificationDisplayState)) {
                    builder = new NotificationCompat.Builder(applicationContext, notificationChannelProvider.channelId());
                    previousNotificationId = notificationId;
                    previousNotificationDisplayState = notificationDisplayState;
                }

                return notificationCustomizer.customNotificationFrom(builder, notificationPayload);
            }

            private boolean hasChanged(int newNotificationId, NotificationCustomizer.NotificationDisplayState newNotificationDisplayState) {
                return previousNotificationId != newNotificationId || !previousNotificationDisplayState.equals(newNotificationDisplayState);
            }

            @Override
            public NotificationCustomizer.NotificationDisplayState notificationDisplayState() {
                return notificationCustomizer.notificationDisplayState(notificationPayload);
            }
        };
    }
}
