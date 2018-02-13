package com.novoda.downloadmanager;

import android.app.NotificationChannel;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

@RequiresApi(Build.VERSION_CODES.O)
class NotificationCreatorOreo<T> extends NotificationCreator<T> {

    private final NotificationChannel notificationChannel;

    public NotificationCreatorOreo(Context context,
                                   String channelId,
                                   NotificationCustomizer<T> customizer,
                                   NotificationChannel notificationChannel) {
        super(context, channelId, customizer);
        this.notificationChannel = notificationChannel;
    }

    @Override
    NotificationChannel createNotificationChannel() {
        return notificationChannel;
    }
}
