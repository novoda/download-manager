package com.novoda.downloadmanager.notifications;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

public interface NotificationInitialiser {
    NotificationCompat.Builder initNotificationBuilder(Context context, NotificationManager notificationManager);
}
