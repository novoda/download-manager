package com.novoda.downloadmanager.lib;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.novoda.downloadmanager.notifications.NotificationChannelProvider;

class DefaultChannelProvider implements NotificationChannelProvider {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public NotificationChannel getNotificationChannel() {
        return new NotificationChannel("App channel id", "App name", NotificationManager.IMPORTANCE_LOW);
    }
}
