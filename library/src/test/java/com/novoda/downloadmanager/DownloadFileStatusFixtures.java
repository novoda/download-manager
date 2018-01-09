package com.novoda.downloadmanager;

class DownloadFileStatusFixtures {

    private DownloadBatchId downloadBatchId = DownloadBatchIdCreator.createFrom("batch_01");
    private DownloadFileId downloadFileId = DownloadFileIdCreator.createFrom("01");
    private InternalDownloadFileStatus.Status status = InternalDownloadFileStatus.Status.QUEUED;
    private FileSize fileSize = InternalFileSizeFixtures.aFileSize().build();
    private FilePath localFilePath = FilePathCreator.unknownFilePath();
    private long downloadDateTimeInMillis = 123456789L;

    static DownloadFileStatusFixtures aDownloadFileStatus() {
        return new DownloadFileStatusFixtures();
    }

    public DownloadFileStatusFixtures withDownloadBatchId(DownloadBatchId downloadBatchId) {
        this.downloadBatchId = downloadBatchId;
        return this;
    }

    DownloadFileStatusFixtures withDownloadFileId(DownloadFileId downloadFileId) {
        this.downloadFileId = downloadFileId;
        return this;
    }

    DownloadFileStatusFixtures withStatus(InternalDownloadFileStatus.Status status) {
        this.status = status;
        return this;
    }

    DownloadFileStatusFixtures withFileSize(FileSize fileSize) {
        this.fileSize = fileSize;
        return this;
    }

    public DownloadFileStatusFixtures withLocalFilePath(FilePath localFilePath) {
        this.localFilePath = localFilePath;
        return this;
    }

    public DownloadFileStatusFixtures withDownloadDateTimeInMillis(long downloadDateTimeInMillis) {
        this.downloadDateTimeInMillis = downloadDateTimeInMillis;
        return this;
    }

    InternalDownloadFileStatus build() {
        return new LiteDownloadFileStatus(
                downloadBatchId,
                downloadFileId,
                status,
                fileSize,
                localFilePath,
                downloadDateTimeInMillis
        );
    }
}
