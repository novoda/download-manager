package com.novoda.downloadmanager;

import android.support.annotation.WorkerThread;

class NotificationDispatcher<T> {

    private final Object waitForDownloadService;
    private final NotificationCreator<T> notificationCreator;

    private DownloadService downloadService;

    NotificationDispatcher(Object waitForDownloadService,
                           NotificationCreator<T> notificationCreator) {
        this.waitForDownloadService = waitForDownloadService;
        this.notificationCreator = notificationCreator;
    }

    @WorkerThread
    void updateNotification(T payload) {
        Wait.<Void>waitFor(downloadService, waitForDownloadService)
                .thenPerform(executeUpdateNotification(payload));
    }

    private Wait.ThenPerform.Action<Void> executeUpdateNotification(T downloadBatchStatus) {
        return () -> {
            NotificationInformation notificationInformation = notificationCreator.createNotification(downloadBatchStatus);

            downloadService.dismissStackedNotification(notificationInformation);

            switch (notificationInformation.notificationStackState()) {
                case SINGLE_PERSISTENT_NOTIFICATION:
                    downloadService.updateNotification(notificationInformation);
                    break;
                case STACK_NOTIFICATION_NOT_DISMISSIBLE:
                    downloadService.stackNotificationNotDismissible(notificationInformation);
                    break;
                case STACK_NOTIFICATION_DISMISSIBLE:
                    downloadService.stackNotification(notificationInformation);
                    break;
                default:
                    String message = String.format(
                            "%s: %s is not supported.",
                            NotificationCustomizer.NotificationStackState.class.getSimpleName(),
                            notificationInformation.notificationStackState()
                    );
                    throw new IllegalArgumentException(message);
            }

            return null;
        };
    }

    void setDownloadService(DownloadService downloadService) {
        this.downloadService = downloadService;
    }
}
