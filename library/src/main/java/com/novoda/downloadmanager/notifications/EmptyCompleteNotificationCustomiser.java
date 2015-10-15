package com.novoda.downloadmanager.notifications;

import android.support.v4.app.NotificationCompat;

import com.novoda.downloadmanager.Download;

public class EmptyCompleteNotificationCustomiser implements CompleteNotificationCustomiser {
    @Override
    public void customiseComplete(Download download, NotificationCompat.Builder builder) {
        // customise nothing
    }
}
