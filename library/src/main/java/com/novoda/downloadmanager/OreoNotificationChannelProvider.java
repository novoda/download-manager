package com.novoda.downloadmanager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

@RequiresApi(Build.VERSION_CODES.O)
class OreoNotificationChannelProvider implements NotificationChannelProvider {

    private final NotificationChannel notificationChannel;

    OreoNotificationChannelProvider(NotificationChannel notificationChannel) {
        this.notificationChannel = notificationChannel;
    }

    @Override
    public void registerNotificationChannel(Context context) {
        notificationManager(context).createNotificationChannel(notificationChannel);
    }

    @Override
    public String channelId() {
        return notificationChannel.getId();
    }

    private static NotificationManager notificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
}
