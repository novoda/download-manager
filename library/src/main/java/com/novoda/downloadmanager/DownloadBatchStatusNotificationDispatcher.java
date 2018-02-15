package com.novoda.downloadmanager;

import com.novoda.notils.logger.simple.Log;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DOWNLOADED;

class DownloadBatchStatusNotificationDispatcher {

    private static final boolean NOTIFICATION_SEEN = true;
    private final DownloadsNotificationSeenPersistence notificationSeenPersistence;
    private final NotificationDispatcher<DownloadBatchStatus> notificationDispatcher;

    DownloadBatchStatusNotificationDispatcher(DownloadsNotificationSeenPersistence notificationSeenPersistence,
                                              NotificationDispatcher<DownloadBatchStatus> notificationDispatcher) {
        this.notificationSeenPersistence = notificationSeenPersistence;
        this.notificationDispatcher = notificationDispatcher;
    }

    void updateNotification(DownloadBatchStatus downloadBatchStatus) {
        if (downloadBatchStatus.notificationSeen()) {
            Log.v("DownloadBatchStatus:", downloadBatchStatus.getDownloadBatchId(), "notification has already been seen.");
            return;
        }

        if (downloadBatchStatus.status() == DOWNLOADED) {
            notificationSeenPersistence.updateNotificationSeenAsync(downloadBatchStatus.getDownloadBatchId(), NOTIFICATION_SEEN);
        }

        notificationDispatcher.updateNotification(downloadBatchStatus);
    }

    void setDownloadService(DownloadService downloadService) {
        notificationDispatcher.setDownloadService(downloadService);
    }
}
