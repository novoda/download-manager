package com.novoda.downloadmanager.notifications;

import com.novoda.downloadmanager.lib.DownloadBatch;
import com.novoda.downloadmanager.lib.NotificationsCreatedListener;

import java.util.Collection;

public interface DownloadNotifier {
    int TYPE_ACTIVE = 1;
    int TYPE_WAITING = 2;
    int TYPE_SUCCESS = 3;
    int TYPE_FAILED = 4;
    int TYPE_CANCELLED = 5;

    void cancelAll();

    void notifyDownloadSpeed(long id, long bytesPerSecond);

    void updateWith(Collection<DownloadBatch> batches, NotificationsCreatedListener notificationsCreatedListener);

}
