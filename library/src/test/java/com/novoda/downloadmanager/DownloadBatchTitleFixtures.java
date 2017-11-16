package com.novoda.downloadmanager;

class DownloadBatchTitleFixtures {

    private String rawBatchTitle = "rawBatchTitle";

    static DownloadBatchTitleFixtures aDownloadBatchTitle() {
        return new DownloadBatchTitleFixtures();
    }

    DownloadBatchTitleFixtures withRawBatchTitle(String rawBatchTitle) {
        this.rawBatchTitle = rawBatchTitle;
        return this;
    }

    DownloadBatchTitle build() {
        return new DownloadBatchTitle() {
            @Override
            public String asString() {
                return rawBatchTitle;
            }
        };
    }
}
