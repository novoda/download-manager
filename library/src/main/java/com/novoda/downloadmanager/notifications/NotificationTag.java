package com.novoda.downloadmanager.notifications;

import android.app.Notification;

import com.novoda.downloadmanager.lib.DownloadBatch;

/**
 * Build tag used for collapsing several {@link DownloadBatch} into a single
 * {@link Notification}.
 */
public class NotificationTag {

    private final int status;
    private final String identifier;

    public static NotificationTag create(DownloadBatch batch, String packageName) {
        if (batch.isQueuedForWifi()) {
            return new NotificationTag(DownloadNotifier.TYPE_WAITING, packageName);
        } else if (batch.isRunning() && batch.shouldShowActiveItem()) {
            return new NotificationTag(DownloadNotifier.TYPE_ACTIVE, packageName);
        } else if (batch.isError() && !batch.isCancelled() && batch.shouldShowCompletedItem()) {
            // Failed downloads always have unique notifications
            return new NotificationTag(DownloadNotifier.TYPE_FAILED, String.valueOf(batch.getBatchId()));
        } else if (batch.isCancelled() && batch.shouldShowCompletedItem()) {
            // Cancelled downloads always have unique notifications
            return new NotificationTag(DownloadNotifier.TYPE_CANCELLED, String.valueOf(batch.getBatchId()));
        } else if (batch.isSuccess() && batch.shouldShowCompletedItem()) {
            // Complete downloads always have unique notifications
            return new NotificationTag(DownloadNotifier.TYPE_SUCCESS, String.valueOf(batch.getBatchId()));
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

    public int status() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NotificationTag that = (NotificationTag) o;

        if (status != that.status) {
            return false;
        }
        return identifier != null ? identifier.equals(that.identifier) : that.identifier == null;

    }

    @Override
    public int hashCode() {
        int result = status;
        result = 31 * result + (identifier != null ? identifier.hashCode() : 0);
        return result;
    }
}
