package com.novoda.downloadmanager;

import android.app.NotificationChannel;
import android.os.Build;
import android.support.annotation.RequiresApi;

interface NotificationChannelCreator {

    @RequiresApi(Build.VERSION_CODES.O)
    NotificationChannel createNotificationChannel();

    String getNotificationChannelId();

}
