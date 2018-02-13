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
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //noinspection ConstantConditions
        notificationManager.createNotificationChannel(notificationChannel);
    }

    @Override
    public String channelId() {
        return notificationChannel.getId();
    }

}
