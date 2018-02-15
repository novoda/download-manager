package com.novoda.downloadmanager;

interface DownloadService extends ToWaitFor {

    void download(DownloadBatch downloadBatch, DownloadBatchStatusCallback callback);

    void updateNotification(NotificationInformation notificationInformation);

    void stackNotification(NotificationInformation notificationInformation);

    void stackNotificationNotDismissible(NotificationInformation notificationInformation);

    void dismissStackedNotification(NotificationInformation notificationInformation);
}
