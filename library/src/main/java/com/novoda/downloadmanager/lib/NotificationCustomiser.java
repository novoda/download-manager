package com.novoda.downloadmanager.lib;

import android.support.v4.app.NotificationCompat;

import com.novoda.downloadmanager.CancelledNotificationCustomiser;
import com.novoda.downloadmanager.CompleteNotificationCustomiser;
import com.novoda.downloadmanager.Download;
import com.novoda.downloadmanager.DownloadingNotificationCustomiser;
import com.novoda.downloadmanager.FailedNotificationCustomiser;
import com.novoda.downloadmanager.QueuedNotificationCustomiser;

class NotificationCustomiser implements QueuedNotificationCustomiser, DownloadingNotificationCustomiser,
        CompleteNotificationCustomiser, CancelledNotificationCustomiser, FailedNotificationCustomiser {

    private final QueuedNotificationCustomiser queuedCustomiser;
    private final DownloadingNotificationCustomiser downloadingCustomiser;
    private final CompleteNotificationCustomiser completeCustomiser;
    private final CancelledNotificationCustomiser cancelledCustomiser;
    private final FailedNotificationCustomiser failedCustomiser;

    public NotificationCustomiser(QueuedNotificationCustomiser queuedCustomiser,
                                  DownloadingNotificationCustomiser downloadingCustomiser,
                                  CompleteNotificationCustomiser completeCustomiser,
                                  CancelledNotificationCustomiser cancelledCustomiser, FailedNotificationCustomiser failedCustomiser) {
        this.queuedCustomiser = queuedCustomiser;
        this.downloadingCustomiser = downloadingCustomiser;
        this.completeCustomiser = completeCustomiser;
        this.cancelledCustomiser = cancelledCustomiser;
        this.failedCustomiser = failedCustomiser;
    }

    @Override
    public void customiseQueued(Download download, NotificationCompat.Builder builder) {
        queuedCustomiser.customiseQueued(download, builder);
    }

    @Override
    public void customiseDownloading(Download download, NotificationCompat.Builder builder) {
        downloadingCustomiser.customiseDownloading(download, builder);
    }

    @Override
    public void customiseComplete(Download download, NotificationCompat.Builder builder) {
        completeCustomiser.customiseComplete(download, builder);
    }

    @Override
    public void customiseCancelled(Download download, NotificationCompat.Builder builder) {
        cancelledCustomiser.customiseCancelled(download, builder);
    }

    @Override
    public void customiseFailed(Download download, NotificationCompat.Builder builder) {
        failedCustomiser.customiseFailed(download, builder);
    }
}
