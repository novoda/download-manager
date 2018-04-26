package com.novoda.downloadmanager;

public interface DownloadsFilePersisted {

    DownloadBatchId downloadBatchId();

    FilePath filePath();

    long totalFileSize();

    String url();

    DownloadFileId downloadFileId();

    FilePersistenceType filePersistenceType();
}
