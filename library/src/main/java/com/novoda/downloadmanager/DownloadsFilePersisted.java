package com.novoda.downloadmanager;

public interface DownloadsFilePersisted {

    DownloadBatchId downloadBatchId();

    FileName fileName();

    FilePath filePath();

    long totalFileSize();

    String url();

    LiteDownloadFileId downloadFileId();

    FilePersistenceType filePersistenceType();
}
