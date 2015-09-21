package com.novoda.downloadmanager.lib;

import android.support.v4.app.NotificationCompat;

public interface NotificationCustomiser {

    void modifyQueuedOrDownloadingNotification(NotificationCompat.Builder builder, long batchId);
}
