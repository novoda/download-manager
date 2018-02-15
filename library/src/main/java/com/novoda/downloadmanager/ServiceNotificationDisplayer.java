package com.novoda.downloadmanager;

import android.app.Notification;
import android.app.Service;
import android.support.v4.app.NotificationManagerCompat;

class ServiceNotificationDisplayer<T> {

    private static final String NOTIFICATION_TAG = "download-manager";

    private final NotificationManagerCompat notificationManager;
    private Service service;

    ServiceNotificationDisplayer(NotificationManagerCompat notificationManager) {
        this.notificationManager = notificationManager;
    }

    void updateNotification(NotificationInformation notificationInformation) {
        service.startForeground(notificationInformation.getId(), notificationInformation.getNotification());
    }

    void stackNotification(NotificationInformation notificationInformation) {
        service.stopForeground(true);
        Notification notification = notificationInformation.getNotification();
        notificationManager.notify(NOTIFICATION_TAG, notificationInformation.getId(), notification);
    }

    void stackNotificationNotDismissible(NotificationInformation notificationInformation) {
        service.stopForeground(true);
        Notification notification = notificationInformation.getNotification();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(NOTIFICATION_TAG, notificationInformation.getId(), notification);
    }

    void dismissStackedNotification(NotificationInformation notificationInformation) {
        notificationManager.cancel(NOTIFICATION_TAG, notificationInformation.getId());
    }

    void setService(T service) {
        if (service instanceof Service) {
            this.service = (Service) service;
        } else {
            String message = String.format(
                    "Parameter: %s is not an instance of %s",
                    service.getClass().getSimpleName(),
                    Service.class.getSimpleName()
            );
            throw new IllegalArgumentException(message);
        }
    }
}
