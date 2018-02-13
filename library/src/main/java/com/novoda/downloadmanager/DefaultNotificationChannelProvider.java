package com.novoda.downloadmanager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

class DefaultNotificationChannelProvider implements NotificationChannelProvider {

    private final String channelId;
    private final String name;
    private final int importance;

    DefaultNotificationChannelProvider(String channelId, String name, @Importance int importance) {
        this.channelId = channelId;
        this.name = name;
        this.importance = importance;
    }

    @Override
    @RequiresApi(Build.VERSION_CODES.O)
    public void registerNotificationChannel(Context context) {
        NotificationChannel notificationChannel = new NotificationChannel(channelId, name, importance);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //noinspection ConstantConditions
        notificationManager.createNotificationChannel(notificationChannel);
    }

    @Override
    public String channelId() {
        return channelId;
    }

}
