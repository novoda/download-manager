package com.novoda.downloadmanager;

import static com.novoda.downloadmanager.DownloadBatchIdFixtures.aDownloadBatchId;
import static com.novoda.downloadmanager.DownloadFileIdFixtures.aDownloadFileId;
import static com.novoda.downloadmanager.DownloadFileStatusFixtures.aDownloadFileStatus;
import static com.novoda.downloadmanager.FileNameFixtures.aFileName;
import static com.novoda.downloadmanager.FilePathFixtures.aFilePath;
import static com.novoda.downloadmanager.FilePersistenceFixtures.aFilePersistence;
import static com.novoda.downloadmanager.InternalFileSizeFixtures.aFileSize;
import static org.mockito.Mockito.mock;

class DownloadFileFixtures {

    private DownloadBatchId downloadBatchId = aDownloadBatchId().build();
    private DownloadFileId downloadFileId = aDownloadFileId().build();
    private String url = "http://example.com";
    private InternalDownloadFileStatus downloadFileStatus = aDownloadFileStatus().build();
    private FileName fileName = aFileName().build();
    private FilePath filePath = aFilePath().build();
    private InternalFileSize fileSize = aFileSize().build();
    private FileDownloader fileDownloader = mock(FileDownloader.class);
    private FileSizeRequester fileSizeRequester = new InMemoryFileSizeRequester();
    private FilePersistence filePersistence = aFilePersistence().build();
    private DownloadsFilePersistence downloadsFilePersistence = mock(DownloadsFilePersistence.class);

    static DownloadFileFixtures aDownloadFile() {
        return new DownloadFileFixtures();
    }

    DownloadFileFixtures withDownloadBatchId(DownloadBatchId downloadBatchId) {
        this.downloadBatchId = downloadBatchId;
        return this;
    }

    DownloadFileFixtures withDownloadFileId(DownloadFileId downloadFileId) {
        this.downloadFileId = downloadFileId;
        return this;
    }

    DownloadFileFixtures withUrl(String url) {
        this.url = url;
        return this;
    }

    DownloadFileFixtures withDownloadFileStatus(InternalDownloadFileStatus downloadFileStatus) {
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
                downloadFileId,
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
