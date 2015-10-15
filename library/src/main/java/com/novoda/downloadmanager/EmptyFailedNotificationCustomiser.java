package com.novoda.downloadmanager;

import android.support.v4.app.NotificationCompat;

public class EmptyFailedNotificationCustomiser implements FailedNotificationCustomiser {
    @Override
    public void customiseFailed(Download download, NotificationCompat.Builder builder) {
        // customise nothing
    }
}
