package com.novoda.downloadmanager;

public interface NotificationCreator {

    NotificationInformation createNotification(String notificationChannelName,
                                               DownloadBatchTitle downloadBatchTitle,
                                               int percentageDownloaded,
                                               int bytesFileSize,
                                               int bytesDownloaded);
}
