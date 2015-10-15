package com.novoda.downloadmanager;

import android.support.v4.app.NotificationCompat;

public class EmptyCompleteNotificationCustomiser implements CompleteNotificationCustomiser {
    @Override
    public void customiseComplete(Download download, NotificationCompat.Builder builder) {
        // customise nothing
    }
}
