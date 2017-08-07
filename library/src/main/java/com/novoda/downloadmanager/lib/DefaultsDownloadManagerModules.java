package com.novoda.downloadmanager.lib;

import android.content.Context;
import android.support.annotation.Nullable;

import com.novoda.downloadmanager.notifications.CancelButtonNotificationCustomiser;
import com.novoda.downloadmanager.notifications.CancelledNotificationCustomiser;
import com.novoda.downloadmanager.notifications.CompleteNotificationCustomiser;
import com.novoda.downloadmanager.notifications.DownloadingNotificationCustomiser;
import com.novoda.downloadmanager.notifications.EmptyCancelledNotificationCustomiser;
import com.novoda.downloadmanager.notifications.EmptyCompleteNotificationCustomiser;
import com.novoda.downloadmanager.notifications.EmptyFailedNotificationCustomiser;
import com.novoda.downloadmanager.notifications.FailedNotificationCustomiser;
import com.novoda.downloadmanager.notifications.NotificationImageRetriever;
import com.novoda.downloadmanager.notifications.OkHttpNotificationImageRetriever;
import com.novoda.downloadmanager.notifications.QueuedNotificationCustomiser;

class DefaultsDownloadManagerModules implements DownloadManagerModules {

    private final Context context;
    @Nullable
    private final QueuedNotificationCustomiser queuedCustomiser;
    @Nullable
    private final DownloadingNotificationCustomiser downloadingCustomiser;
    @Nullable
    private final CompleteNotificationCustomiser completeCustomiser;
    @Nullable
    private final CancelledNotificationCustomiser cancelledCustomiser;
    @Nullable
    private final FailedNotificationCustomiser failedCustomiser;
    @Nullable
    private final DownloadClientReadyChecker readyChecker;
    @Nullable
    private final NotificationImageRetriever imageRetriever;
    @Nullable
    private final NotificationChannelCustomiser channelCustomiser;
    @Nullable
    private final DestroyListener destroyListener;

    DefaultsDownloadManagerModules(Context context) {
        this(context, null, null, null, null, null, null, null, null, null);
    }

    public DefaultsDownloadManagerModules(Context context,
                                          @Nullable QueuedNotificationCustomiser queuedCustomiser,
                                          @Nullable DownloadingNotificationCustomiser downloadingCustomiser,
                                          @Nullable CompleteNotificationCustomiser completeCustomiser,
                                          @Nullable CancelledNotificationCustomiser cancelledCustomiser,
                                          @Nullable FailedNotificationCustomiser failedCustomiser,
                                          @Nullable DownloadClientReadyChecker readyChecker,
                                          @Nullable NotificationImageRetriever imageRetriever,
                                          @Nullable NotificationChannelCustomiser channelCustomiser,
                                          @Nullable DestroyListener destroyListener) {
        this.context = context;
        this.queuedCustomiser = queuedCustomiser;
        this.downloadingCustomiser = downloadingCustomiser;
        this.completeCustomiser = completeCustomiser;
        this.cancelledCustomiser = cancelledCustomiser;
        this.failedCustomiser = failedCustomiser;
        this.readyChecker = readyChecker;
        this.imageRetriever = imageRetriever;
        this.channelCustomiser = channelCustomiser;
        this.destroyListener = destroyListener;
    }

    @Override
    public NotificationImageRetriever getNotificationImageRetriever() {
        if (imageRetriever == null) {
            return new OkHttpNotificationImageRetriever();
        }
        return imageRetriever;
    }

    @Override
    public DownloadClientReadyChecker getDownloadClientReadyChecker() {
        if (readyChecker == null) {
            return DownloadClientReadyChecker.READY;
        }
        return readyChecker;
    }

    @Override
    public QueuedNotificationCustomiser getQueuedNotificationCustomiser() {
        if (queuedCustomiser == null) {
            return new CancelButtonNotificationCustomiser(context);
        }
        return queuedCustomiser;
    }

    @Override
    public DownloadingNotificationCustomiser getDownloadingNotificationCustomiser() {
        if (downloadingCustomiser == null) {
            return new CancelButtonNotificationCustomiser(context);
        }
        return downloadingCustomiser;
    }

    @Override
    public CompleteNotificationCustomiser getCompleteNotificationCustomiser() {
        if (completeCustomiser == null) {
            return new EmptyCompleteNotificationCustomiser();
        }
        return completeCustomiser;
    }

    @Override
    public CancelledNotificationCustomiser getCancelledNotificationCustomiser() {
        if (cancelledCustomiser == null) {
            return new EmptyCancelledNotificationCustomiser();
        }
        return cancelledCustomiser;
    }

    @Override
    public FailedNotificationCustomiser getFailedNotificationCustomiser() {
        if (failedCustomiser == null) {
            return new EmptyFailedNotificationCustomiser();
        }
        return failedCustomiser;
    }

    @Override
    public NotificationChannelCustomiser getNotificationChannelCustomiser() {
        return channelCustomiser;
    }

    @Override
    public DestroyListener getDestroyListener() {
        if (destroyListener == null) {
            return new DestroyListener.NoOp();
        }
        return destroyListener;
    }

}
