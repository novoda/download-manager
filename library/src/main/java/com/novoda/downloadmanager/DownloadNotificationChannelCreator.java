package com.novoda.downloadmanager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

class DownloadNotificationChannelCreator implements NotificationChannelCreator {

    private final NotificationManager notificationManager;

    DownloadNotificationChannelCreator(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    private static String CHANNEL_ID;

    @Override
    public String createDownloadNotificationChannel(Context context) {
        if (CHANNEL_ID == null) {
            CHANNEL_ID = "download-manager";
            createNotificationChannelForAndroidOreo(context);
        }

        return CHANNEL_ID;
    }

    private void createNotificationChannelForAndroidOreo(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "Download Manager Notification Service";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
