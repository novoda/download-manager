package com.novoda.downloadmanager.notifications;

public interface NotificationCustomiserProvider {
    QueuedNotificationCustomiser getQueuedNotificationCustomiser();

    DownloadingNotificationCustomiser getDownloadingNotificationCustomiser();

    CompleteNotificationCustomiser getCompleteNotificationCustomiser();

    CancelledNotificationCustomiser getCancelledNotificationCustomiser();

    FailedNotificationCustomiser getFailedNotificationCustomiser();
}
