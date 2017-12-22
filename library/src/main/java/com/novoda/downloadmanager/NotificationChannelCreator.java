package com.novoda.downloadmanager;

import android.app.NotificationChannel;

interface NotificationChannelCreator {

    Optional<NotificationChannel> createNotificationChannel();

    String getNotificationChannelId();

}
