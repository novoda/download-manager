package com.novoda.downloadmanager;

import android.support.annotation.WorkerThread;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DELETION;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DOWNLOADED;

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

            if (downloadBatchStatus.status() == DOWNLOADED || downloadBatchStatus.status() == DELETION) {
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
