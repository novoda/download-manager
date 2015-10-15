package com.novoda.downloadmanager;

import android.support.v4.app.NotificationCompat;

import com.novoda.downloadmanager.Download;

public interface DownloadingNotificationCustomiser {

    void customiseDownloading(Download download, NotificationCompat.Builder builder);

}
