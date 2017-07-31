package com.novoda.downloadmanager.notifications;

import android.app.Notification;

import com.novoda.downloadmanager.lib.DownloadBatch;

import java.util.Collection;

public interface DownloadNotifier {
    void cancelAll();

    void notifyDownloadSpeed(long id, long bytesPerSecond);

    void updateWith(Collection<DownloadBatch> batches, NotificationCreatedCallback notificationCreatedCallback);

    interface NotificationCreatedCallback {
        void onNotificationCreated(NotificationTag tag, Notification notification);
    }
}
