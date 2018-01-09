package com.novoda.downloadmanager;

class DownloadBatchIdFixtures {

    private String rawDownloadBatchId = "rawDownloadBatchId";

    static DownloadBatchIdFixtures aDownloadBatchId() {
        return new DownloadBatchIdFixtures();
    }

    DownloadBatchIdFixtures withRawDownloadBatchId(String rawDownloadBatchId) {
        this.rawDownloadBatchId = rawDownloadBatchId;
        return this;
    }

    DownloadBatchId build() {
        return () -> rawDownloadBatchId;
    }
}
