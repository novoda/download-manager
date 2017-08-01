package com.novoda.downloadmanager.notifications;

import android.app.Notification;
import android.app.Service;
import android.support.v4.util.SimpleArrayMap;

import static com.novoda.downloadmanager.notifications.SynchronisedDownloadNotifier.TYPE_ACTIVE;

public class NotificationsCreatedListener {
    private Service service;

    public NotificationsCreatedListener(Service service) {
        this.service = service;
    }

    void onNotificationCreated(SimpleArrayMap<NotificationTag, Notification> taggedNotifications) {
        boolean noActiveDownloads = true;

        for (int i = 0; i < taggedNotifications.size(); i++) {
            NotificationTag currentTag = taggedNotifications.keyAt(i);
            if (currentTag.status() == TYPE_ACTIVE) {
                service.startForeground(currentTag.hashCode(), taggedNotifications.get(currentTag));
                noActiveDownloads = false;
                break;
            }
        }
        if (noActiveDownloads) {
            service.stopForeground(false);
        }
    }
}
