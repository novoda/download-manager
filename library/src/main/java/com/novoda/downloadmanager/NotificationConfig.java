package com.novoda.downloadmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class NotificationConfig<T> implements NotificationChannelCreator, NotificationCreator<T> {

    private final Context applicationContext;
    private final String channelId;
    private final String userFacingChannelDescription;
    private final NotificationCustomiser<T> notificationCustomiser;
    private final int importance;

    public NotificationConfig(Context context, String channelId, String userFacingChannelDescription, NotificationCustomiser<T> customiser, int importance) {
        this.applicationContext = context.getApplicationContext();
        this.channelId = channelId;
        this.userFacingChannelDescription = userFacingChannelDescription;
        this.notificationCustomiser = customiser;
        this.importance = importance;
    }

    NotificationInformation notificationInformation(final T payload) {
        return new NotificationInformation() {
            @Override
            public int getId() {
                return payload.hashCode();
            }

            @Override
            public Notification getNotification() {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(applicationContext, channelId);
                return notificationCustomiser.customNotificationFrom(builder, payload);
            }
        };
    }

    Optional<NotificationChannel> notificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Optional.of(new NotificationChannel(channelId, userFacingChannelDescription, importance));
        }
        return Optional.absent();
    }

    @Override
    public NotificationInformation createNotification(String notificationChannelName, T notificationPayload) {
        return notificationInformation(notificationPayload);
    }

    @Override
    public Optional<NotificationChannel> createNotificationChannel() {
        return notificationChannel();
    }

    @Override
    public String getNotificationChannelId() {
        return channelId;
    }
}
