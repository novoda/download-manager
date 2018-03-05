package com.novoda.downloadmanager;

interface NotificationCreator<T> {

    void setNotificationChannelProvider(NotificationChannelProvider notificationChannelProvider);

    NotificationInformation createNotification(T notificationPayload);
}
