package com.novoda.downloadmanager;

public final class DownloadBatchTitleCreator {

    private DownloadBatchTitleCreator() {
        // Uses static factory methods.
    }

    public static DownloadBatchTitle createFrom(Batch batch) {
        return new LiteDownloadBatchTitle(batch.title());
    }

    public static DownloadBatchTitle createFrom(String title) {
        return new LiteDownloadBatchTitle(title);
    }
}
