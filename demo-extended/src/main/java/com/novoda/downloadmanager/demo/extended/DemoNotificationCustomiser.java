package com.novoda.downloadmanager.demo.extended;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.novoda.downloadmanager.Download;
import com.novoda.downloadmanager.notifications.DownloadingNotificationCustomiser;
import com.novoda.downloadmanager.notifications.QueuedNotificationCustomiser;
import com.novoda.downloadmanager.demo.R;
import com.novoda.downloadmanager.lib.DownloadManager;

class DemoNotificationCustomiser implements QueuedNotificationCustomiser, DownloadingNotificationCustomiser {

    private final Context context;

    public DemoNotificationCustomiser(Context context) {
        this.context = context;
    }

    @Override
    public void customiseQueued(Download download, NotificationCompat.Builder builder) {
        addViewAction(builder);
        addCancelAction(builder, download);
    }

    @Override
    public void customiseDownloading(Download download, NotificationCompat.Builder builder) {
        addViewAction(builder);
        addCancelAction(builder, download);
    }

    private void addViewAction(NotificationCompat.Builder builder) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action action = new NotificationCompat.Action(R.drawable.ic_notif_info, "View", pendingIntent);
        builder.addAction(action);
    }

    private void addCancelAction(NotificationCompat.Builder builder, Download download) {
        Intent cancelIntent = DownloadManager.createCancelBatchIntent(download.getId(), context);
        PendingIntent pendingCancelIntent = PendingIntent.getBroadcast(context, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(com.novoda.downloadmanager.R.drawable.dl__ic_action_cancel, context.getString(com.novoda.downloadmanager.R.string.dl__cancel), pendingCancelIntent);
    }
}
