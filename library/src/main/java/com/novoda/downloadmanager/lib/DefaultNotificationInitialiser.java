package com.novoda.downloadmanager.lib;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.novoda.downloadmanager.notifications.NotificationInitialiser;

class DefaultNotificationInitialiser implements NotificationInitialiser {
    @Override
    public NotificationCompat.Builder initNotificationBuilder(Context context, NotificationManager notificationManager) {
        return new NotificationCompat.Builder(context);
    }
}
