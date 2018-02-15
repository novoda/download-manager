package com.novoda.downloadmanager;

import android.app.Service;

import com.novoda.notils.logger.simple.Log;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DOWNLOADED;

class DownloadBatchStatusNotificationDispatcher {

    private static final boolean NOTIFICATION_SEEN = true;
    private final DownloadsNotificationSeenPersistence notificationSeenPersistence;
    private final ServiceNotificationDispatcher<DownloadBatchStatus> notificationDispatcher;

    DownloadBatchStatusNotificationDispatcher(DownloadsNotificationSeenPersistence notificationSeenPersistence,
                                              ServiceNotificationDispatcher<DownloadBatchStatus> notificationDispatcher) {
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
        if (downloadService instanceof Service) {
            notificationDispatcher.setService((Service) downloadService);
        } else {
            String message = String.format(
                    "Parameter: %s does not resolve to %s",
                    downloadService.getClass().getSimpleName(),
                    Service.class.getSimpleName()
            );
            throw new IllegalArgumentException(message);
        }
    }
}
