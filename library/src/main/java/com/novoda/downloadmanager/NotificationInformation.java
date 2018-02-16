package com.novoda.downloadmanager;

import android.app.Notification;

interface NotificationInformation {

    int getId();

    Notification getNotification();

    NotificationCustomizer.NotificationDisplayState notificationDisplayState();
}
