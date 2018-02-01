package com.novoda.downloadmanager;

interface DownloadService {

    void download(DownloadBatch downloadBatch, DownloadBatchStatusCallback callback);

    void updateNotification(NotificationInformation notificationInformation);

    void stackNotification(NotificationInformation notificationInformation);

    void stackNotificationNotDismissible(NotificationInformation notificationInformation);

    void dismissStackedNotification(NotificationInformation notificationInformation);
}
