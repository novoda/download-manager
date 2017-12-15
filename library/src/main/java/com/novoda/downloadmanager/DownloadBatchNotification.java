package com.novoda.downloadmanager;

import android.app.Notification;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.v4.app.NotificationCompat;

class DownloadBatchNotification implements NotificationCreator<DownloadBatchStatus> {

    private static final boolean NOT_INDETERMINATE = false;

    private final Context context;
    private final int iconDrawable;

    DownloadBatchNotification(Context context, @DrawableRes int iconDrawable) {
        this.context = context.getApplicationContext();
        this.iconDrawable = iconDrawable;
    }

    @Override
    public NotificationInformation createNotification(String notificationChannelName, DownloadBatchStatus downloadBatchStatus) {
        DownloadBatchTitle downloadBatchTitle = downloadBatchStatus.getDownloadBatchTitle();
        int percentageDownloaded = downloadBatchStatus.percentageDownloaded();
        int bytesFileSize = (int) downloadBatchStatus.bytesTotalSize();
        int bytesDownloaded = (int) downloadBatchStatus.bytesDownloaded();
        String title = downloadBatchTitle.asString();
        String content = percentageDownloaded + "% downloaded";

        Notification notification = new NotificationCompat.Builder(context, notificationChannelName)
                .setProgress(bytesFileSize, bytesDownloaded, NOT_INDETERMINATE)
                .setSmallIcon(iconDrawable)
                .setContentTitle(title)
                .setContentText(content)
                .build();

        return new DownloadBatchNotificationInformation(downloadBatchTitle.hashCode(), notification);
    }

    private static class DownloadBatchNotificationInformation implements NotificationInformation {
        private final int id;
        private final Notification notification;

        DownloadBatchNotificationInformation(int id, Notification notification) {
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
