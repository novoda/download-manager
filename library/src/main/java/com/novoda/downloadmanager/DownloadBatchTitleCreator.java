package com.novoda.downloadmanager;

class DownloadBatchTitleCreator {

    static DownloadBatchTitle createFrom(Batch batch) {
        return new LiteDownloadBatchTitle(batch.getTitle());
    }

    static DownloadBatchTitle createFrom(String title) {
        return new LiteDownloadBatchTitle(title);
    }
}
