package com.novoda.downloadmanager;

import java.util.Set;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DELETED;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DOWNLOADED;

class DownloadBatchStatusNotificationDispatcher {

    private static final boolean NOTIFICATION_SEEN = true;

    private final DownloadsNotificationSeenPersistence notificationSeenPersistence;
    private final ServiceNotificationDispatcher<DownloadBatchStatus> notificationDispatcher;
    private final Set<String> downloadBatchIdNotificationSeen;

    DownloadBatchStatusNotificationDispatcher(DownloadsNotificationSeenPersistence notificationSeenPersistence,
                                              ServiceNotificationDispatcher<DownloadBatchStatus> notificationDispatcher,
                                              Set<String> downloadBatchIdNotificationSeen) {
        this.notificationSeenPersistence = notificationSeenPersistence;
        this.notificationDispatcher = notificationDispatcher;
        this.downloadBatchIdNotificationSeen = downloadBatchIdNotificationSeen;
    }

    void updateNotification(DownloadBatchStatus downloadBatchStatus) {
        if (downloadBatchStatus.notificationSeen()) {
            Logger.v("DownloadBatchStatus:", downloadBatchStatus.getDownloadBatchId(), "notification has already been seen.");
            return;
        }

        String rawDownloadBatchId = downloadBatchStatus.getDownloadBatchId().rawId();

        if (downloadBatchStatus.status() == DELETED) {
            downloadBatchIdNotificationSeen.remove(rawDownloadBatchId);
        }

        if (notificationIsNotMarkedAsSeenYet(downloadBatchStatus, rawDownloadBatchId)) {
            downloadBatchIdNotificationSeen.add(rawDownloadBatchId);
            Logger.v("start updateNotificationSeenAsync " + rawDownloadBatchId
                    + ", seen: " + NOTIFICATION_SEEN
                    + ", status: " + downloadBatchStatus.status());
            notificationSeenPersistence.updateNotificationSeenAsync(downloadBatchStatus, NOTIFICATION_SEEN);
        }

        notificationDispatcher.updateNotification(downloadBatchStatus);
    }

    private boolean notificationIsNotMarkedAsSeenYet(DownloadBatchStatus downloadBatchStatus, String rawDownloadBatchId) {
        return downloadBatchStatus.status() == DOWNLOADED && !downloadBatchIdNotificationSeen.contains(rawDownloadBatchId);
    }

    void setDownloadService(DownloadService downloadService) {
        notificationDispatcher.setService(downloadService);
    }
}
