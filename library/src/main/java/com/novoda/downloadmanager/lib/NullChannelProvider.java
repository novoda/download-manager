package com.novoda.downloadmanager.lib;

import android.app.NotificationChannel;

import com.novoda.downloadmanager.notifications.NotificationChannelProvider;

class NullChannelProvider implements NotificationChannelProvider {
    @Override
    public NotificationChannel getNotificationChannel() {
        return null;
    }
}
