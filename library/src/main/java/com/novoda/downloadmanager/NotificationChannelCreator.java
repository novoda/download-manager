package com.novoda.downloadmanager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class NotificationChannelCreator {

    public static String createDownloadNotificationChannel(Context context) {
        if (CHANNEL_ID == null) {
            CHANNEL_ID = "download-manager";
            createNotificationChannelForAndroidOreo(context);
        }

        return CHANNEL_ID;
    }

    private static void createNotificationChannelForAndroidOreo(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "Download Manager Notification Service";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private static String CHANNEL_ID;

}
