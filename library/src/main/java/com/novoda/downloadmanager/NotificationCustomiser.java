package com.novoda.downloadmanager;

import android.app.Notification;
import android.support.v4.app.NotificationCompat;

public interface NotificationCustomiser<T> {
    Notification customNotificationFrom(NotificationCompat.Builder builder, T payload);
}
