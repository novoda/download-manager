package com.novoda.downloadmanager.demo;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.novoda.downloadmanager.DownloadBatchStatus;
import com.novoda.downloadmanager.DownloadBatchTitle;
import com.novoda.downloadmanager.NotificationChannelCreator;
import com.novoda.downloadmanager.NotificationCreator;
import com.novoda.downloadmanager.NotificationInformation;
import com.novoda.notils.logger.simple.Log;

public class CustomNotificationCreator implements NotificationCreator<DownloadBatchStatus> {

    private static final int ID = 1;
    private static final boolean NOT_INDETERMINATE = false;

    private final Context context;
    private final int iconDrawable;
    private final NotificationChannelCreator notificationChannelCreator;

    public CustomNotificationCreator(Context context, int iconDrawable, NotificationChannelCreator notificationChannelCreator) {
        this.context = context;
        this.iconDrawable = iconDrawable;
        this.notificationChannelCreator = notificationChannelCreator;
    }

    @Override
    public NotificationInformation createNotification(String notificationChannelName, DownloadBatchStatus downloadBatchStatus) {
        DownloadBatchTitle downloadBatchTitle = downloadBatchStatus.getDownloadBatchTitle();
        int percentageDownloaded = downloadBatchStatus.percentageDownloaded();
        int bytesFileSize = (int) downloadBatchStatus.bytesTotalSize();
        int bytesDownloaded = (int) downloadBatchStatus.bytesDownloaded();
        String title = downloadBatchTitle.asString();
        String content = percentageDownloaded + "% downloaded";

        Log.v("Create notification for " + title + ", " + content);

        String notificationChannel = notificationChannelCreator.getNotificationChannelName();

        Notification notification = new NotificationCompat.Builder(context, notificationChannel)
                .setProgress(bytesFileSize, bytesDownloaded, NOT_INDETERMINATE)
                .setSmallIcon(iconDrawable)
                .setContentTitle(title)
                .setContentText(content)
                .build();

        return new CustomNotificationInformation(ID, notification);
    }

    private static class CustomNotificationInformation implements NotificationInformation {

        private final int id;
        private final Notification notification;

        CustomNotificationInformation(int id, Notification notification) {
            this.id = id;
            this.notification = notification;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public Notification getNotification() {
            return notification;
        }
    }
}
