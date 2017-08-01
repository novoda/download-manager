package com.novoda.downloadmanager.notifications;

import android.app.Notification;
import android.app.Service;
import android.support.v4.util.SimpleArrayMap;

import static com.novoda.downloadmanager.notifications.SynchronisedDownloadNotifier.TYPE_ACTIVE;

/**
 * Listens for creation of {@link android.app.Notification} objects
 * and puts its given {@link android.app.Service} instance in the foreground or background,
 * depending on the presence of active downloads.
 * This class is only temporarily public and is not intended for client use.
 */
public class NotificationsCreatedListener {
    private final Service service;

    public NotificationsCreatedListener(Service service) {
        this.service = service;
    }

    void onNotificationsCreated(SimpleArrayMap<NotificationTag, Notification> taggedNotifications) {
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
