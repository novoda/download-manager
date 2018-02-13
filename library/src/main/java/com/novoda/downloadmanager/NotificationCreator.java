package com.novoda.downloadmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

public abstract class NotificationCreator<T> {

    private final Context applicationContext;
    private final String channelId;
    private final NotificationCustomizer<T> notificationCustomizer;

    public static <T> NotificationCreator<T> create(Context context,
                                             String channelId,
                                             String userFacingChannelDescription,
                                             @Importance int importance,
                                             NotificationCustomizer<T> customizer) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, userFacingChannelDescription, importance);
            return new NotificationCreatorOreo<>(context, channelId, customizer, channel);
        } else {
            return new NotificationCreatorDefault<>(context, channelId, customizer);
        }
    }

    NotificationCreator(Context context,
                        String channelId,
                        NotificationCustomizer<T> customizer) {
        this.applicationContext = context.getApplicationContext();
        this.channelId = channelId;
        this.notificationCustomizer = customizer;
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
    abstract NotificationChannel createNotificationChannel();
}
