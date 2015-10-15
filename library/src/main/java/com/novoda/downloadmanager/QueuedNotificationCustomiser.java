package com.novoda.downloadmanager;

import android.support.v4.app.NotificationCompat;

public interface QueuedNotificationCustomiser {

    void customiseQueued(Download download, NotificationCompat.Builder builder);

}
