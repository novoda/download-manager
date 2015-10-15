package com.novoda.downloadmanager.lib;

import android.content.Context;

import com.novoda.downloadmanager.CancelledNotificationCustomiser;
import com.novoda.downloadmanager.CompleteNotificationCustomiser;
import com.novoda.downloadmanager.DownloadingNotificationCustomiser;
import com.novoda.downloadmanager.EmptyCancelledNotificationCustomiser;
import com.novoda.downloadmanager.EmptyCompleteNotificationCustomiser;
import com.novoda.downloadmanager.EmptyFailedNotificationCustomiser;
import com.novoda.downloadmanager.FailedNotificationCustomiser;
import com.novoda.downloadmanager.QueuedNotificationCustomiser;

class DefaultsDownloadManagerModules implements DownloadManagerModules {

    private final Context context;

    public DefaultsDownloadManagerModules(Context context) {
        this.context = context;
    }

    @Override
    public NotificationImageRetriever getNotificationImageRetriever() {
        return new OkHttpNotificationImageRetriever();
    }

    @Override
    public DownloadClientReadyChecker getDownloadClientReadyChecker() {
        return DownloadClientReadyChecker.READY;
    }

    @Override
    public QueuedNotificationCustomiser getQueuedNotificationCustomiser() {
        return new CancelButtonNotificationCustomiser(context);
    }

    @Override
    public DownloadingNotificationCustomiser getDownloadingNotificationCustomiser() {
        return new CancelButtonNotificationCustomiser(context);
    }

    @Override
    public CompleteNotificationCustomiser getCompleteNotificationCustomiser() {
        return new EmptyCompleteNotificationCustomiser();
    }

    @Override
    public CancelledNotificationCustomiser getCancelledNotificationCustomiser() {
        return new EmptyCancelledNotificationCustomiser();
    }

    @Override
    public FailedNotificationCustomiser getFailedNotificationCustomiser() {
        return new EmptyFailedNotificationCustomiser();
    }
}
