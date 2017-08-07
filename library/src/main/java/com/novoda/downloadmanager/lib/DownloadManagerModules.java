package com.novoda.downloadmanager.lib;

import android.content.Context;
import android.support.annotation.NonNull;

import com.novoda.downloadmanager.notifications.CancelledNotificationCustomiser;
import com.novoda.downloadmanager.notifications.CompleteNotificationCustomiser;
import com.novoda.downloadmanager.notifications.DownloadingNotificationCustomiser;
import com.novoda.downloadmanager.notifications.FailedNotificationCustomiser;
import com.novoda.downloadmanager.notifications.NotificationImageRetriever;
import com.novoda.downloadmanager.notifications.QueuedNotificationCustomiser;

public interface DownloadManagerModules {

    NotificationImageRetriever getNotificationImageRetriever();

    DownloadClientReadyChecker getDownloadClientReadyChecker();

    QueuedNotificationCustomiser getQueuedNotificationCustomiser();

    DownloadingNotificationCustomiser getDownloadingNotificationCustomiser();

    CompleteNotificationCustomiser getCompleteNotificationCustomiser();

    CancelledNotificationCustomiser getCancelledNotificationCustomiser();

    FailedNotificationCustomiser getFailedNotificationCustomiser();

    NotificationChannelCustomiser getNotificationChannelCustomiser();

    DestroyListener getDestroyListener();

    class Builder {

        private final Context context;

        private QueuedNotificationCustomiser queuedCustomiser;
        private DownloadingNotificationCustomiser downloadingCustomiser;
        private CompleteNotificationCustomiser completeCustomiser;
        private CancelledNotificationCustomiser cancelledCustomiser;
        private FailedNotificationCustomiser failedCustomiser;
        private DownloadClientReadyChecker readyChecker;
        private NotificationImageRetriever imageRetriever;
        private NotificationChannelCustomiser channelCustomiser;
        private DestroyListener destroyListener;

        public static Builder from(@NonNull Context context) {
            return new Builder(context.getApplicationContext());
        }

        Builder(Context context) {
            this.context = context;
        }

        public Builder withQueuedNotificationCustomiser(@NonNull QueuedNotificationCustomiser queuedCustomiser) {
            this.queuedCustomiser = queuedCustomiser;
            return this;
        }

        public Builder withDownloadingNotificationCustomiser(@NonNull DownloadingNotificationCustomiser downloadingCustomiser) {
            this.downloadingCustomiser = downloadingCustomiser;
            return this;
        }

        public Builder withCompleteNotificationCustomiser(@NonNull CompleteNotificationCustomiser completeCustomiser) {
            this.completeCustomiser = completeCustomiser;
            return this;
        }

        public Builder withCancelledNotificationCustomiser(@NonNull CancelledNotificationCustomiser cancelledCustomiser) {
            this.cancelledCustomiser = cancelledCustomiser;
            return this;
        }

        public Builder withFailedNotificationCustomiser(@NonNull FailedNotificationCustomiser failedCustomiser) {
            this.failedCustomiser = failedCustomiser;
            return this;
        }

        public Builder withDownloadClientReadyChecker(@NonNull DownloadClientReadyChecker readyChecker) {
            this.readyChecker = readyChecker;
            return this;
        }

        public Builder withNotificationImageRetrieverFactory(@NonNull NotificationImageRetriever imageRetriever) {
            this.imageRetriever = imageRetriever;
            return this;
        }

        public Builder withNotificationChannelCustomiser(@NonNull NotificationChannelCustomiser channelCustomiser) {
            this.channelCustomiser = channelCustomiser;
            return this;
        }

        public Builder withDestroyListener(@NonNull DestroyListener destroyListener) {
            this.destroyListener = destroyListener;
            return this;
        }

        public DownloadManagerModules build() {
            return new DefaultsDownloadManagerModules(
                    context,
                    queuedCustomiser,
                    downloadingCustomiser,
                    completeCustomiser,
                    cancelledCustomiser,
                    failedCustomiser,
                    readyChecker,
                    imageRetriever,
                    channelCustomiser,
                    destroyListener
            );
        }

    }

    interface Provider {
        DownloadManagerModules provideDownloadManagerModules();
    }
}
