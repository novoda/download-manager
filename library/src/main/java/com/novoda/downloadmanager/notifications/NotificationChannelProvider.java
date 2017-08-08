package com.novoda.downloadmanager.notifications;

import android.app.NotificationChannel;
import android.os.Build;
import android.support.annotation.RequiresApi;

public interface NotificationChannelProvider {
    @RequiresApi(api = Build.VERSION_CODES.O)
    NotificationChannel getNotificationChannel();
}
