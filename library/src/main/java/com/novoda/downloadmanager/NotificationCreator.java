package com.novoda.downloadmanager;

public interface NotificationCreator {

    NotificationInformation createNotificationWithProgress(String notificationChannelName,
                                                           DownloadBatchTitle downloadBatchTitle,
                                                           int percentageDownloaded,
                                                           int bytesFileSize,
                                                           int bytesDownloaded);
}
