package com.novoda.downloadmanager;

public interface NotificationCreator<T> {

    NotificationInformation createNotification(String notificationChannelName, T notificationPayload);

}
