package com.novoda.downloadmanager;

public interface NotificationCustomiserProvider {
    QueuedNotificationCustomiser getQueuedNotificationCustomiser();

    DownloadingNotificationCustomiser getDownloadingNotificationCustomiser();

    CompleteNotificationCustomiser getCompleteNotificationCustomiser();

    CancelledNotificationCustomiser getCancelledNotificationCustomiser();

    FailedNotificationCustomiser getFailedNotificationCustomiser();
}
