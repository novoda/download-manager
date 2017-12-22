package com.novoda.downloadmanager;

import android.app.NotificationChannel;
import android.os.Build;
import android.support.annotation.RequiresApi;

interface NotificationMetadata<T> {
    NotificationInformation createNotification(T notificationPayload);

    @RequiresApi(Build.VERSION_CODES.O)
    NotificationChannel createNotificationChannel();
}
