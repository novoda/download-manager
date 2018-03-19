package com.novoda.downloadmanager;

interface DownloadsNotificationSeenPersistence {

    void updateNotificationSeenAsync(DownloadBatchStatus downloadBatchStatus, boolean notificationSeen);
}
