package com.novoda.downloadmanager;

import android.app.NotificationChannel;

public interface NotificationChannelCreator {

    Optional<NotificationChannel> createNotificationChannel();

    String getNotificationChannelId();

}
