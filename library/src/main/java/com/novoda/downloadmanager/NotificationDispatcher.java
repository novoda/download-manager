package com.novoda.downloadmanager;

import android.support.annotation.WorkerThread;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DELETION;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DOWNLOADED;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.ERROR;

class NotificationDispatcher {

    private final Object waitForDownloadService;
    private final NotificationCreator<DownloadBatchStatus> notificationCreator;

    private DownloadService downloadService;

    NotificationDispatcher(Object waitForDownloadService, NotificationCreator<DownloadBatchStatus> notificationCreator) {
        this.waitForDownloadService = waitForDownloadService;
        this.notificationCreator = notificationCreator;
    }

    @WorkerThread
    void updateNotification(DownloadBatchStatus downloadBatchStatus) {
        WaitForDownloadService.<Void>waitFor(downloadService, waitForDownloadService)
                .thenPerform(executeUpdateNotification(downloadBatchStatus));
    }

    private WaitForDownloadService.ThenPerform.Action<Void> executeUpdateNotification(DownloadBatchStatus downloadBatchStatus) {
        return () -> {
            NotificationInformation notificationInformation = notificationCreator.createNotification(downloadBatchStatus);
            DownloadBatchStatus.Status status = downloadBatchStatus.status();

            if (status == DOWNLOADED || status == DELETION || status == ERROR) {
                downloadService.stackNotification(notificationInformation);
            } else {
                downloadService.updateNotification(notificationInformation);
            }

            return null;
        };
    }

    void setDownloadService(DownloadService downloadService) {
        this.downloadService = downloadService;
    }
}
