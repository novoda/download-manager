package com.novoda.downloadmanager.lib;

import android.app.NotificationManager;

import com.novoda.downloadmanager.notifications.NotificationManagerCustomiser;

class NonCustomisingNotificationManagerCustomiser implements NotificationManagerCustomiser {
    @Override
    public NotificationManager customise(NotificationManager notificationManager) {
        return notificationManager;
    }
}
