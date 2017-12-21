package com.novoda.downloadmanager;

interface NotificationCreator<T> {

    NotificationInformation createNotification(String notificationChannelName, T notificationPayload);

}
