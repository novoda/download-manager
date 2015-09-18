package com.novoda.downloadmanager.demo.extended;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.novoda.downloadmanager.demo.R;
import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.lib.NotificationCustomiser;

class DemoNotificationCustomiser implements NotificationCustomiser {

    private final Context context;

    public DemoNotificationCustomiser(Context context) {
        this.context = context;
    }

    @Override
    public void addActionsForBatch(NotificationCompat.Builder builder, long batchId) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action action = new NotificationCompat.Action(R.drawable.ic_notif_info, "View", pendingIntent);
        builder.addAction(action);
        Intent cancelIntent = DownloadManager.createCancelBatchIntent(batchId, context);
        PendingIntent pendingCancelIntent = PendingIntent.getBroadcast(context, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(com.novoda.downloadmanager.R.drawable.dl__ic_action_cancel, context.getString(com.novoda.downloadmanager.R.string.dl__cancel), pendingCancelIntent);
    }

}
