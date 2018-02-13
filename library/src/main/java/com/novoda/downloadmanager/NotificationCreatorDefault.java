package com.novoda.downloadmanager;

import android.app.NotificationChannel;
import android.content.Context;

import com.novoda.notils.exception.DeveloperError;

class NotificationCreatorDefault<T> extends NotificationCreator<T> {

    public NotificationCreatorDefault(Context context, String channelId, NotificationCustomizer<T> customizer) {
        super(context, channelId, customizer);
    }

    @Override
    NotificationChannel createNotificationChannel() {
        throw new DeveloperError("Notification Channel can only be created with Android Oreo");
    }
}
