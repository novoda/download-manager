package com.novoda.downloadmanager.lib;

import android.content.Context;

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

    class Builder {

        private final Context context;

        private QueuedNotificationCustomiser queuedCustomiser;
        private DownloadingNotificationCustomiser downloadingCustomiser;
        private CompleteNotificationCustomiser completeCustomiser;
        private CancelledNotificationCustomiser cancelledCustomiser;
        private FailedNotificationCustomiser failedCustomiser;
        private DownloadClientReadyChecker readyChecker;
        private NotificationImageRetriever imageRetriever;

        public static Builder from(Context context) {
            return new Builder(context.getApplicationContext());
        }

        Builder(Context context) {
            this.context = context;
        }

        public Builder withQueuedNotificationCustomiser(QueuedNotificationCustomiser queuedCustomiser) {
            this.queuedCustomiser = queuedCustomiser;
            return this;
        }

        public Builder withDownloadingNotificationCustomiser(DownloadingNotificationCustomiser downloadingCustomiser) {
            this.downloadingCustomiser = downloadingCustomiser;
            return this;
        }

        public Builder withCompleteNotificationCustomiser(CompleteNotificationCustomiser completeCustomiser) {
            this.completeCustomiser = completeCustomiser;
            return this;
        }

        public Builder withCancelledNotificationCustomiser(CancelledNotificationCustomiser cancelledCustomiser) {
            this.cancelledCustomiser = cancelledCustomiser;
            return this;
        }

        public Builder withFailedNotificationCustomiser(FailedNotificationCustomiser failedCustomiser) {
            this.failedCustomiser = failedCustomiser;
            return this;
        }

        public Builder withDownloadClientReadyChecker(DownloadClientReadyChecker readyChecker) {
            this.readyChecker = readyChecker;
            return this;
        }

        public Builder withNotificationImageRetrieverFactory(NotificationImageRetriever imageRetriever) {
            this.imageRetriever = imageRetriever;
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
                    imageRetriever
            );
        }

    }

    interface Provider {
        DownloadManagerModules provideDownloadManagerModules();
    }
}
