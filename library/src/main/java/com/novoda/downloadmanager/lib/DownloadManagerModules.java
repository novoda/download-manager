package com.novoda.downloadmanager.lib;

import android.content.Context;

import com.novoda.downloadmanager.CancelledNotificationCustomiser;
import com.novoda.downloadmanager.CompleteNotificationCustomiser;
import com.novoda.downloadmanager.DownloadingNotificationCustomiser;
import com.novoda.downloadmanager.FailedNotificationCustomiser;
import com.novoda.downloadmanager.QueuedNotificationCustomiser;

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
            DefaultsDownloadManagerModules defaultsDownloadManagerModules = new DefaultsDownloadManagerModules(context);
            if (queuedCustomiser == null) {
                queuedCustomiser = defaultsDownloadManagerModules.getQueuedNotificationCustomiser();
            }
            if (downloadingCustomiser == null) {
                downloadingCustomiser = defaultsDownloadManagerModules.getDownloadingNotificationCustomiser();
            }
            if (completeCustomiser == null) {
                completeCustomiser = defaultsDownloadManagerModules.getCompleteNotificationCustomiser();
            }
            if (cancelledCustomiser == null) {
                cancelledCustomiser = defaultsDownloadManagerModules.getCancelledNotificationCustomiser();
            }
            if (failedCustomiser == null) {
                failedCustomiser = defaultsDownloadManagerModules.getFailedNotificationCustomiser();
            }
            if (readyChecker == null) {
                readyChecker = defaultsDownloadManagerModules.getDownloadClientReadyChecker();
            }
            if (imageRetriever == null) {
                imageRetriever = defaultsDownloadManagerModules.getNotificationImageRetriever();
            }
            return new DownloadManagerModules() {
                @Override
                public NotificationImageRetriever getNotificationImageRetriever() {
                    return imageRetriever;
                }

                @Override
                public DownloadClientReadyChecker getDownloadClientReadyChecker() {
                    return readyChecker;
                }

                @Override
                public QueuedNotificationCustomiser getQueuedNotificationCustomiser() {
                    return queuedCustomiser;
                }

                @Override
                public DownloadingNotificationCustomiser getDownloadingNotificationCustomiser() {
                    return downloadingCustomiser;
                }

                @Override
                public CompleteNotificationCustomiser getCompleteNotificationCustomiser() {
                    return completeCustomiser;
                }

                @Override
                public CancelledNotificationCustomiser getCancelledNotificationCustomiser() {
                    return cancelledCustomiser;
                }

                @Override
                public FailedNotificationCustomiser getFailedNotificationCustomiser() {
                    return failedCustomiser;
                }
            };
        }

    }

    interface Provider {
        DownloadManagerModules provideDownloadManagerModules();
    }
}
