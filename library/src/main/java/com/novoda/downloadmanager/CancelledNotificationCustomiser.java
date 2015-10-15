package com.novoda.downloadmanager;

import android.support.v4.app.NotificationCompat;

public interface CancelledNotificationCustomiser {

    void customiseCancelled(Download download, NotificationCompat.Builder builder);

}
