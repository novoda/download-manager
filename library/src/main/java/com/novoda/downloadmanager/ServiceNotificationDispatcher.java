package com.novoda.downloadmanager;

import android.app.Notification;
import android.support.annotation.WorkerThread;
import android.support.v4.app.NotificationManagerCompat;

class ServiceNotificationDispatcher<T> {

    private static final String NOTIFICATION_TAG = "download-manager";

    private final Object waitForDownloadService;
    private final NotificationCreator<T> notificationCreator;
    private final NotificationManagerCompat notificationManager;

    private DownloadManagerService service;
    private int persistentNotificationId;

    ServiceNotificationDispatcher(Object waitForDownloadService,
                                  NotificationCreator<T> notificationCreator,
                                  NotificationManagerCompat notificationManager) {
        this.waitForDownloadService = waitForDownloadService;
        this.notificationCreator = notificationCreator;
        this.notificationManager = notificationManager;
    }

    @WorkerThread
    void updateNotification(T payload) {
        Wait.<Void>waitFor(service, waitForDownloadService)
                .thenPerform(executeUpdateNotification(payload));
    }

    private Wait.ThenPerform.Action<Void> executeUpdateNotification(T payload) {
        return () -> {
            NotificationInformation notificationInformation = notificationCreator.createNotification(payload);

            dismissStackedNotification(notificationInformation);

            switch (notificationInformation.notificationDisplayState()) {
                case SINGLE_PERSISTENT_NOTIFICATION:
                    updatePersistentNotification(notificationInformation);
                    break;
                case STACK_NOTIFICATION_NOT_DISMISSIBLE:
                    stackNotificationNotDismissible(notificationInformation);
                    break;
                case STACK_NOTIFICATION_DISMISSIBLE:
                    stackNotification(notificationInformation);
                    break;
                case HIDDEN_NOTIFICATION:
                    dismissPersistentIfCurrent(notificationInformation);
                    break;
                default:
                    String message = String.format(
                            "%s: %s is not supported.",
                            NotificationCustomizer.NotificationDisplayState.class.getSimpleName(),
                            notificationInformation.notificationDisplayState()
                    );
                    throw new IllegalArgumentException(message);
            }

            return null;
        };
    }

    private void dismissStackedNotification(NotificationInformation notificationInformation) {
        notificationManager.cancel(NOTIFICATION_TAG, notificationInformation.getId());
    }

    private void updatePersistentNotification(NotificationInformation notificationInformation) {
        persistentNotificationId = notificationInformation.getId();
        service.start(notificationInformation.getId(), notificationInformation.getNotification());
    }

    private void stackNotification(NotificationInformation notificationInformation) {
        dismissPersistentIfCurrent(notificationInformation);
        Notification notification = notificationInformation.getNotification();
        notificationManager.notify(NOTIFICATION_TAG, notificationInformation.getId(), notification);
    }

    private void stackNotificationNotDismissible(NotificationInformation notificationInformation) {
        dismissPersistentIfCurrent(notificationInformation);
        Notification notification = notificationInformation.getNotification();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(NOTIFICATION_TAG, notificationInformation.getId(), notification);
    }

    private void dismissPersistentIfCurrent(NotificationInformation notificationInformation) {
        if (persistentNotificationId == notificationInformation.getId()) {
            service.stop(true);
        }
    }

    void setService(DownloadManagerService service) {
        this.service = service;
    }
}
