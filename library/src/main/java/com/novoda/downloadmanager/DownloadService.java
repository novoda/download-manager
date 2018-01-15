package com.novoda.downloadmanager;

interface DownloadService {

    void download(DownloadBatch downloadBatch, DownloadBatchCallback callback);

    void updateNotification(NotificationInformation notificationInformation);

    void stackNotification(NotificationInformation notificationInformation);

    void dismissNotification(NotificationInformation notificationInformation);
}
