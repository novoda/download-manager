package com.novoda.downloadmanager;

import android.app.Notification;

interface DownloadManagerService extends ToWaitFor {
    void start(int id, Notification notification);

    void stop(boolean removeNotification);
}
