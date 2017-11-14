package com.novoda.downloadmanager;

interface DownloadService {

    void download(DownloadBatch downloadBatch, DownloadBatchCallback callback);

    void updateNotification(NotificationInformation notification);

    void stackNotification(NotificationInformation notificationInformation);

    void dismissNotification();
}
