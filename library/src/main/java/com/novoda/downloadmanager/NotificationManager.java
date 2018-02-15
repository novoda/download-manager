package com.novoda.downloadmanager;

import android.app.Notification;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;

class NotificationManager {

    private final NotificationManagerCompat notificationManagerCompat;

    NotificationManager(NotificationManagerCompat notificationManagerCompat) {
        this.notificationManagerCompat = notificationManagerCompat;
    }

    void notify(@Nullable String tag, int id, Notification notification) {
        notificationManagerCompat.notify(tag, id, notification);
    }

    void cancel(@Nullable String tag, int id) {
        notificationManagerCompat.cancel(tag, id);
    }
}
