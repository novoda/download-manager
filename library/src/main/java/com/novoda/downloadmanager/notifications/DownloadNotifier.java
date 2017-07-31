package com.novoda.downloadmanager.notifications;

import android.app.Notification;
import android.support.v4.util.SimpleArrayMap;

import com.novoda.downloadmanager.lib.DownloadBatch;

import java.util.Collection;

public interface DownloadNotifier {
    int TYPE_ACTIVE = 1;
    int TYPE_WAITING = 2;
    int TYPE_SUCCESS = 3;
    int TYPE_FAILED = 4;
    int TYPE_CANCELLED = 5;

    void cancelAll();

    void notifyDownloadSpeed(long id, long bytesPerSecond);

    void updateWith(Collection<DownloadBatch> batches, NotificationsCreatedCallback notificationsCreatedCallback);

    interface NotificationsCreatedCallback {
        void onNotificationCreated(SimpleArrayMap<NotificationTag, Notification> taggedNotifications);
    }
}
