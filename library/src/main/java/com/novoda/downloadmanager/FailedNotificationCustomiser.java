package com.novoda.downloadmanager;

import android.support.v4.app.NotificationCompat;

public interface FailedNotificationCustomiser {

    void customiseFailed(Download download, NotificationCompat.Builder builder);

}
