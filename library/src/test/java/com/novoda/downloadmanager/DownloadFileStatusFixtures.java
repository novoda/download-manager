package com.novoda.downloadmanager;

class DownloadFileStatusFixtures {

    private DownloadFileId downloadFileId = DownloadFileId.from("01");
    private DownloadFileStatus.Status status = DownloadFileStatus.Status.QUEUED;
    private FileSize fileSize = InternalFileSizeFixtures.aFileSize().build();

    static DownloadFileStatusFixtures aDownloadFileStatus() {
        return new DownloadFileStatusFixtures();
    }

    DownloadFileStatusFixtures withDownloadFileId(DownloadFileId downloadFileId) {
        this.downloadFileId = downloadFileId;
        return this;
    }

    DownloadFileStatusFixtures withStatus(DownloadFileStatus.Status status) {
        this.status = status;
        return this;
    }

    DownloadFileStatusFixtures withFileSize(FileSize fileSize) {
        this.fileSize = fileSize;
        return this;
    }

    DownloadFileStatus build() {
        return new DownloadFileStatus(
                downloadFileId,
                status,
                fileSize
        );
    }
}
