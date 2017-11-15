package com.novoda.downloadmanager;

class DownloadFileFixtures {

    private DownloadBatchId downloadBatchId = DownloadBatchIdFixtures.aDownloadBatchId().build();
    private String url = "http://example.com";
    private DownloadFileStatus downloadFileStatus;
    private FileName fileName;
    private FilePath filePath;
    private InternalFileSize fileSize;
    private FileDownloader fileDownloader;
    private FileSizeRequester fileSizeRequester;
    private FilePersistence filePersistence;
    private DownloadsFilePersistence downloadsFilePersistence;

    static DownloadFileFixtures aDownloadFile() {
        return new DownloadFileFixtures();
    }

    DownloadFileFixtures withDownloadBatchId(DownloadBatchId downloadBatchId) {
        this.downloadBatchId = downloadBatchId;
        return this;
    }

    DownloadFileFixtures withUrl(String url) {
        this.url = url;
        return this;
    }

    DownloadFileFixtures withDownloadFileStatus(DownloadFileStatus downloadFileStatus) {
        this.downloadFileStatus = downloadFileStatus;
        return this;
    }

    DownloadFileFixtures withFileName(FileName fileName) {
        this.fileName = fileName;
        return this;
    }

    DownloadFileFixtures withFilePath(FilePath filePath) {
        this.filePath = filePath;
        return this;
    }

    DownloadFileFixtures withFileSize(InternalFileSize fileSize) {
        this.fileSize = fileSize;
        return this;
    }

    DownloadFileFixtures withFileDownloader(FileDownloader fileDownloader) {
        this.fileDownloader = fileDownloader;
        return this;
    }

    DownloadFileFixtures withFileSizeRequester(FileSizeRequester fileSizeRequester) {
        this.fileSizeRequester = fileSizeRequester;
        return this;
    }

    DownloadFileFixtures withFilePersistence(FilePersistence filePersistence) {
        this.filePersistence = filePersistence;
        return this;
    }

    DownloadFileFixtures withDownloadsFilePersistence(DownloadsFilePersistence downloadsFilePersistence) {
        this.downloadsFilePersistence = downloadsFilePersistence;
        return this;
    }

    DownloadFile build() {
        return new DownloadFile(
                downloadBatchId,
                url,
                downloadFileStatus,
                fileName,
                filePath,
                fileSize,
                fileDownloader,
                fileSizeRequester,
                filePersistence,
                downloadsFilePersistence
        );
    }
}
