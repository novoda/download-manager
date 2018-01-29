package com.novoda.downloadmanager;

final class DownloadBatchTitleCreator {

    private DownloadBatchTitleCreator() {
        // Uses static factory methods.
    }

    static DownloadBatchTitle createFrom(Batch batch) {
        return new LiteDownloadBatchTitle(batch.title());
    }

    static DownloadBatchTitle createFrom(String title) {
        return new LiteDownloadBatchTitle(title);
    }
}
