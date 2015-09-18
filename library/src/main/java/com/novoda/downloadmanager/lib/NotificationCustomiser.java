package com.novoda.downloadmanager.lib;

import android.support.v4.app.NotificationCompat;

public interface NotificationCustomiser {

    void addActionsForBatch(NotificationCompat.Builder builder, long batchId);
}
