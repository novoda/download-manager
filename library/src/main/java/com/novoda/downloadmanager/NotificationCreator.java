package com.novoda.downloadmanager;

public interface NotificationCreator {

    NotificationInformation createNotification(DownloadBatchTitle downloadBatchTitle,
                                               int percentageDownloaded,
                                               int bytesFileSize,
                                               int bytesDownloaded);
}
