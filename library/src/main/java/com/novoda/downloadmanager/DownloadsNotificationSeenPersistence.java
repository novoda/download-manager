package com.novoda.downloadmanager;

interface DownloadsNotificationSeenPersistence {

    void updateNotificationSeenAsync(DownloadBatchId downloadBatchId, boolean notificationSeen);
}
