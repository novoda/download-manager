package com.novoda.downloadmanager;

import android.app.Notification;
import android.support.v4.app.NotificationCompat;

public interface NotificationCustomizer<T> {

    NotificationDisplayState notificationDisplayState(T payload);

    Notification customNotificationFrom(NotificationCompat.Builder builder, T payload);

    enum NotificationDisplayState {
        SINGLE_PERSISTENT_NOTIFICATION,
        STACK_NOTIFICATION_NOT_DISMISSIBLE,
        STACK_NOTIFICATION_DISMISSIBLE,
        HIDDEN_NOTIFICATION
    }
}
