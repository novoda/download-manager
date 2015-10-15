package com.novoda.downloadmanager;

import android.support.v4.app.NotificationCompat;

import com.novoda.downloadmanager.Download;

public interface CompleteNotificationCustomiser {

    void customiseComplete(Download download, NotificationCompat.Builder builder);

}
