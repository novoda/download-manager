package com.novoda.downloadmanager;

import static com.novoda.downloadmanager.DownloadBatchIdFixtures.aDownloadBatchId;

final class DownloadsBatchPersistedFixtures {
    private DownloadBatchId downloadBatchId = aDownloadBatchId().build();
    private DownloadBatchStatus.Status downloadBatchStatus = DownloadBatchStatus.Status.DOWNLOADED;
    private DownloadBatchTitle downloadBatchTitle = new LiteDownloadBatchTitle("title");
    private long downloadedDateTimeInMillis = 123456789L;
    private boolean notificationSeen = false;

    static DownloadsBatchPersistedFixtures aDownloadsBatchPersisted() {
        return new DownloadsBatchPersistedFixtures();
    }

    private DownloadsBatchPersistedFixtures() {
        // use aBatch() to get an instance of this class
    }

    DownloadsBatchPersistedFixtures withRawDownloadBatchId(final String rawId) {
        downloadBatchId = aDownloadBatchId().withRawDownloadBatchId(rawId).build();
        return this;
    }

    DownloadsBatchPersistedFixtures withDownloadBatchStatus(DownloadBatchStatus.Status status) {
        this.downloadBatchStatus = status;
        return this;
    }

    DownloadsBatchPersistedFixtures withDownloadBatchTitle(final String title) {
        this.downloadBatchTitle = new DownloadBatchTitle() {
            @Override
            public String asString() {
                return title;
            }
        };
        return this;
    }

    DownloadsBatchPersistedFixtures withDownloadedDateTimeInMillis(long downloadedDateTimeInMillis) {
        this.downloadedDateTimeInMillis = downloadedDateTimeInMillis;
        return this;
    }

    DownloadsBatchPersistedFixtures withNotificationSeen(boolean notificationSeen) {
        this.notificationSeen = notificationSeen;
        return this;
    }

    DownloadsBatchPersisted build() {
        return new DownloadsBatchPersisted() {
            @Override
            public DownloadBatchId downloadBatchId() {
                return downloadBatchId;
            }

            @Override
            public DownloadBatchStatus.Status downloadBatchStatus() {
                return downloadBatchStatus;
            }

            @Override
            public DownloadBatchTitle downloadBatchTitle() {
                return downloadBatchTitle;
            }

            @Override
            public long downloadedDateTimeInMillis() {
                return downloadedDateTimeInMillis;
            }

            @Override
            public boolean notificationSeen() {
                return notificationSeen;
            }
        };
    }
}
