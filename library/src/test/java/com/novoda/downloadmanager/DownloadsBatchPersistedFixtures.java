package com.novoda.downloadmanager;

import static com.novoda.downloadmanager.DownloadBatchIdFixtures.aDownloadBatchId;

final class DownloadsBatchPersistedFixtures {
    private DownloadBatchId downloadBatchId = aDownloadBatchId().build();
    private DownloadBatchStatus.Status downloadBatchStatus = DownloadBatchStatus.Status.DOWNLOADED;
    private DownloadBatchTitle downloadBatchTitle;

    static DownloadsBatchPersistedFixtures aDownloadsBatchPersisted() {
        return new DownloadsBatchPersistedFixtures();
    }

    private DownloadsBatchPersistedFixtures() {
        // use aBatch() to get an instance of this class
    }

    DownloadsBatchPersistedFixtures withDownloadBatchId(final String rawId) {
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
        };
    }
}
