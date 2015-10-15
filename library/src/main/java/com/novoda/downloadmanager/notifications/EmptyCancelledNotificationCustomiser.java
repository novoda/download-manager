package com.novoda.downloadmanager.notifications;

import android.support.v4.app.NotificationCompat;

import com.novoda.downloadmanager.Download;

public class EmptyCancelledNotificationCustomiser implements CancelledNotificationCustomiser {
    @Override
    public void customiseCancelled(Download download, NotificationCompat.Builder builder) {
        // customise nothing
    }
}
