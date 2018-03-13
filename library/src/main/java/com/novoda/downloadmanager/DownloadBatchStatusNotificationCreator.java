package com.novoda.downloadmanager;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

class DownloadBatchStatusNotificationCreator implements NotificationCreator<DownloadBatchStatus> {

    private final Context applicationContext;
    private final NotificationCustomizer<DownloadBatchStatus> notificationCustomizer;
    private NotificationChannelProvider notificationChannelProvider;

    DownloadBatchStatusNotificationCreator(Context context,
                                           NotificationCustomizer<DownloadBatchStatus> customizer,
                                           NotificationChannelProvider notificationChannelProvider) {
        this.applicationContext = context.getApplicationContext();
        this.notificationCustomizer = customizer;
        this.notificationChannelProvider = notificationChannelProvider;
    }

    @Override
    public void setNotificationChannelProvider(NotificationChannelProvider notificationChannelProvider) {
        this.notificationChannelProvider = notificationChannelProvider;
    }

    private int previousNotificationId;
    private DownloadBatchStatus.Status previousStatus;
    private NotificationCompat.Builder builder;

    @Override
    public NotificationInformation createNotification(final DownloadBatchStatus downloadBatchStatus) {
        return new NotificationInformation() {
            @Override
            public int getId() {
                return downloadBatchStatus.getDownloadBatchId().hashCode();
            }

            @Override
            public Notification getNotification() {
                int notificationId = getId();
                DownloadBatchStatus.Status status = downloadBatchStatus.status();

                if (builder == null || hasChanged(previousNotificationId, status)) {
                    builder = new NotificationCompat.Builder(applicationContext, notificationChannelProvider.channelId());
                    previousNotificationId = notificationId;
                    previousStatus = status;
                }

                return notificationCustomizer.customNotificationFrom(builder, downloadBatchStatus);
            }

            private boolean hasChanged(int notificationId, DownloadBatchStatus.Status status) {
                return previousNotificationId != notificationId || !previousStatus.equals(status);
            }

            @Override
            public NotificationCustomizer.NotificationDisplayState notificationDisplayState() {
                return notificationCustomizer.notificationDisplayState(downloadBatchStatus);
            }
        };
    }
}
