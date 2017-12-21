package com.novoda.downloadmanager;

interface NotificationCreator<T> {
    NotificationInformation createNotification(T notificationPayload);
}
