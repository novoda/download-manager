package com.novoda.downloadmanager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.res.Resources;
import android.os.Build;

class DownloadNotificationChannelCreator implements NotificationChannelCreator {

    private final Resources resources;

    DownloadNotificationChannelCreator(Resources resources) {
        this.resources = resources;
    }

    @Override
    public Optional<NotificationChannel> createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelName(), channelDescription(), NotificationManager.IMPORTANCE_LOW);
            return Optional.of(channel);
        }

        return Optional.absent();
    }

    private String channelName() {
        return resources.getString(R.string.notification_channel_name);
    }

    private String channelDescription() {
        return resources.getString(R.string.notification_channel_description);
    }

    @Override
    public String getNotificationChannelName() {
        return channelName();
    }

}
