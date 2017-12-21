package com.novoda.downloadmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

class NotificationConfig<T> {

    private final Context context;
    private final String channelId;
    private final String userFacingChannelName;
    private final NotificationCustomiser<T> notificationCustomiser;
    private final int importance;

    NotificationConfig(Context context, String channelId, String userFacingChannelName, NotificationCustomiser<T> customiser, int importance) {
        this.context = context.getApplicationContext();
        this.channelId = channelId;
        this.userFacingChannelName = userFacingChannelName;
        this.notificationCustomiser = customiser;
        this.importance = importance;
    }

    NotificationInformation notificationInformation(final T payload) {
        return new NotificationInformation() {
            @Override
            public int getId() {
                return notificationCustomiser.notificationId(payload);
            }

            @Override
            public Notification getNotification() {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
                return notificationCustomiser.customNotificationFrom(builder, payload);
            }
        };
    }

    Optional<NotificationChannel> notificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Optional.of(new NotificationChannel(channelId, userFacingChannelName, importance));
        }
        return Optional.absent();
    }
}
