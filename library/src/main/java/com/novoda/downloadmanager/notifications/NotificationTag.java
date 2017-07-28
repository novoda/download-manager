package com.novoda.downloadmanager.notifications;

import com.novoda.downloadmanager.lib.DownloadBatch;

public class NotificationTag {

    private final int status;
    private final String identifier;

    public static NotificationTag create(DownloadBatch batch, String packageName) {
        if (batch.isQueuedForWifi()) {
            return new NotificationTag(SynchronisedDownloadNotifier.TYPE_WAITING, packageName);
        } else if (batch.isRunning() && batch.shouldShowActiveItem()) {
            return new NotificationTag(SynchronisedDownloadNotifier.TYPE_ACTIVE, packageName);
        } else if (batch.isError() && !batch.isCancelled() && batch.shouldShowCompletedItem()) {
            // Failed downloads always have unique notifications
            return new NotificationTag(SynchronisedDownloadNotifier.TYPE_FAILED, String.valueOf(batch.getBatchId()));
        } else if (batch.isCancelled() && batch.shouldShowCompletedItem()) {
            // Cancelled downloads always have unique notifications
            return new NotificationTag(SynchronisedDownloadNotifier.TYPE_CANCELLED, String.valueOf(batch.getBatchId()));
        } else if (batch.isSuccess() && batch.shouldShowCompletedItem()) {
            // Complete downloads always have unique notifications
            return new NotificationTag(SynchronisedDownloadNotifier.TYPE_SUCCESS, String.valueOf(batch.getBatchId()));
        } else {
            return null;
        }
    }

    private NotificationTag(int status, String identifier) {
        this.status = status;
        this.identifier = identifier;
    }

    public String tag() {
        return status + ":" + identifier;
    }
}
