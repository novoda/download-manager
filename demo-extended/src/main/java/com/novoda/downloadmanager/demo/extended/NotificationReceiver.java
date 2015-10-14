package com.novoda.downloadmanager.demo.extended;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.novoda.downloadmanager.lib.DownloadManager;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int[] statuses = intent.getIntArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_STATUSES);
        Toast.makeText(context, "Clicked on a " + getStatusMessage(statuses) + " notification", Toast.LENGTH_LONG).show();
        context.startActivity(new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private String getStatusMessage(int[] statuses) {
        switch (statuses[0]) {
            case DownloadManager.STATUS_SUCCESSFUL:
                return "completed";
            case DownloadManager.STATUS_RUNNING:
                return "downloading";
            case DownloadManager.STATUS_PENDING:
                return "pending";
            case DownloadManager.STATUS_PAUSED:
                return "paused";
            case DownloadManager.STATUS_FAILED:
                return "failed";
            default:
                return "unknown";
        }
    }

}
