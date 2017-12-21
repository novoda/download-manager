package com.novoda.downloadmanager.demo;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.novoda.downloadmanager.DownloadBatchStatus;
import com.novoda.downloadmanager.DownloadBatchTitle;
import com.novoda.downloadmanager.NotificationConfig;
import com.novoda.downloadmanager.NotificationCustomizer;
import com.novoda.notils.logger.simple.Log;

public final class CustomNotificationConfigFactory {

    private static final boolean NOT_INDETERMINATE = false;

    public static NotificationConfig<DownloadBatchStatus> createDownloadNotificationConfig(Context context, String channelId, String channelDescription, final int iconDrawable) {

        NotificationCustomizer<DownloadBatchStatus> notificationCustomizer = new NotificationCustomizer<DownloadBatchStatus>() {
            @Override
            public Notification customNotificationFrom(NotificationCompat.Builder builder, DownloadBatchStatus downloadBatchStatus) {
                DownloadBatchTitle downloadBatchTitle = downloadBatchStatus.getDownloadBatchTitle();
                int percentageDownloaded = downloadBatchStatus.percentageDownloaded();
                int bytesFileSize = (int) downloadBatchStatus.bytesTotalSize();
                int bytesDownloaded = (int) downloadBatchStatus.bytesDownloaded();
                String title = downloadBatchTitle.asString();
                String content = percentageDownloaded + "% downloaded";

                Log.v("Create notification for " + title + ", " + content);

                return builder
                        .setProgress(bytesFileSize, bytesDownloaded, NOT_INDETERMINATE)
                        .setSmallIcon(iconDrawable)
                        .setContentTitle(title)
                        .setContentText(content)
                        .build();

            }
        };
        return new NotificationConfig<>(
                context,
                channelId,
                channelDescription,
                notificationCustomizer,
                NotificationManagerCompat.IMPORTANCE_LOW
        );
    }

}
