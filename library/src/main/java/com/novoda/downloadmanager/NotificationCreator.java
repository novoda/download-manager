package com.novoda.downloadmanager;

public interface NotificationCreator<PAYLOAD> {

    NotificationInformation createNotification(String notificationChannelName, PAYLOAD notificationPayload);

}
