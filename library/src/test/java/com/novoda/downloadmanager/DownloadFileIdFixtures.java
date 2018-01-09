package com.novoda.downloadmanager;

class DownloadFileIdFixtures {

    private String rawDownloadFileId = "rawDownloadFileId";

    static DownloadFileIdFixtures aDownloadFileId() {
        return new DownloadFileIdFixtures();
    }

    DownloadFileIdFixtures withRawDownloadFileId(String rawDownloadFileId) {
        this.rawDownloadFileId = rawDownloadFileId;
        return this;
    }

    DownloadFileId build() {
        return () -> rawDownloadFileId;
    }
}
