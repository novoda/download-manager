package com.novoda.downloadmanager;

import android.support.v4.app.NotificationCompat;

public class EmptyCancelledNotificationCustomiser implements CancelledNotificationCustomiser {
    @Override
    public void customiseCancelled(Download download, NotificationCompat.Builder builder) {
        // customise nothing
    }
}
