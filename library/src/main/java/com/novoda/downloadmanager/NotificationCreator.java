package com.novoda.downloadmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

public class NotificationCreator<T> {

    private final Context applicationContext;
    private final String channelId;
    private final String userFacingChannelDescription;
    private final NotificationCustomizer<T> notificationCustomizer;
    @Importance
    private final int importance;

    public NotificationCreator(Context context,
                               String channelId,
                               String userFacingChannelDescription,
                               @Importance int importance,
                               NotificationCustomizer<T> customizer) {
        this.applicationContext = context.getApplicationContext();
        this.channelId = channelId;
        this.userFacingChannelDescription = userFacingChannelDescription;
        this.notificationCustomizer = customizer;
        this.importance = importance;
    }

    NotificationInformation createNotification(final T notificationPayload) {
        return new NotificationInformation() {
            @Override
            public int getId() {
                if (notificationPayload instanceof DownloadBatchStatus) {
                    return ((DownloadBatchStatus) notificationPayload).getDownloadBatchId().hashCode();
                }

                return notificationPayload.hashCode();
            }

            @Override
            public Notification getNotification() {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(applicationContext, channelId);
                return notificationCustomizer.customNotificationFrom(builder, notificationPayload);
            }
        };
    }

    @RequiresApi(Build.VERSION_CODES.O)
    NotificationChannel createNotificationChannel() {
        return new NotificationChannel(channelId, userFacingChannelDescription, importance);
    }
}
