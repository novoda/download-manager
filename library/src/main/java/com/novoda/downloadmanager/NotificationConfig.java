package com.novoda.downloadmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

class NotificationConfig<T> implements NotificationMetadata<T> {

    private final Context applicationContext;
    private final String channelId;
    private final String userFacingChannelDescription;
    private final NotificationCustomizer<T> notificationCustomizer;
    @Importance
    private final int importance;

    NotificationConfig(Context context,
                       String channelId,
                       String userFacingChannelDescription,
                       NotificationCustomizer<T> customizer,
                       @Importance int importance) {
        this.applicationContext = context.getApplicationContext();
        this.channelId = channelId;
        this.userFacingChannelDescription = userFacingChannelDescription;
        this.notificationCustomizer = customizer;
        this.importance = importance;
    }

    @Override
    public NotificationInformation createNotification(final T notificationPayload) {
        return new NotificationInformation() {
            @Override
            public int getId() {
                return notificationPayload.hashCode();
            }

            @Override
            public Notification getNotification() {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(applicationContext, channelId);
                return notificationCustomizer.customNotificationFrom(builder, notificationPayload);
            }
        };
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Override
    public NotificationChannel createNotificationChannel() {
        return new NotificationChannel(channelId, userFacingChannelDescription, importance);
    }
}
