package com.novoda.downloadmanager.lib;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.novoda.downloadmanager.CancelledNotificationCustomiser;
import com.novoda.downloadmanager.CompleteNotificationCustomiser;
import com.novoda.downloadmanager.Download;
import com.novoda.downloadmanager.DownloadingNotificationCustomiser;
import com.novoda.downloadmanager.FailedNotificationCustomiser;
import com.novoda.downloadmanager.QueuedNotificationCustomiser;
import com.novoda.downloadmanager.R;

class DefaultNotificationCustomiser implements QueuedNotificationCustomiser, DownloadingNotificationCustomiser,
        CompleteNotificationCustomiser, CancelledNotificationCustomiser, FailedNotificationCustomiser {

    private final Context context;

    public DefaultNotificationCustomiser(Context context) {
        this.context = context;
    }

    @Override
    public void customiseQueued(Download download, NotificationCompat.Builder builder) {
        addCancelButton(builder, download);
    }

    @Override
    public void customiseDownloading(Download download, NotificationCompat.Builder builder) {
        addCancelButton(builder, download);
    }

    private void addCancelButton(NotificationCompat.Builder builder, Download download) {
        Intent cancelIntent = DownloadManager.createCancelBatchIntent(download.getId(), context);
        PendingIntent pendingCancelIntent = PendingIntent.getBroadcast(context, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.dl__ic_action_cancel, context.getString(R.string.dl__cancel), pendingCancelIntent);
    }

    @Override
    public void customiseCancelled(Download download, NotificationCompat.Builder builder) {
        // no-op
    }

    @Override
    public void customiseComplete(Download download, NotificationCompat.Builder builder) {
        // no-op
    }

    @Override
    public void customiseFailed(Download download, NotificationCompat.Builder builder) {
        // no-op
    }

}
