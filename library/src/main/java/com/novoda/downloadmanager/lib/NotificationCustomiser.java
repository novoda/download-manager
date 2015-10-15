package com.novoda.downloadmanager.lib;

import android.support.v4.app.NotificationCompat;

import com.novoda.downloadmanager.Download;

public interface NotificationCustomiser {

    void modifyQueuedOrDownloadingNotification(NotificationCompat.Builder builder, Download download);
}
