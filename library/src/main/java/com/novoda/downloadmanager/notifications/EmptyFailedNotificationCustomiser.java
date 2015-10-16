package com.novoda.downloadmanager.notifications;

import android.support.v4.app.NotificationCompat;

import com.novoda.downloadmanager.Download;

public class EmptyFailedNotificationCustomiser implements FailedNotificationCustomiser {
    @Override
    public void customiseFailed(Download download, NotificationCompat.Builder builder) {
        // customise nothing
    }
}
