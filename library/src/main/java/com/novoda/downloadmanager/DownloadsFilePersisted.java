package com.novoda.downloadmanager;

/**
 * Defines the information that is stored in the persistence layer for a {@link BatchFile}.
 */
public interface DownloadsFilePersisted {

    DownloadBatchId downloadBatchId();

    FilePath filePath();

    long totalFileSize();

    String url();

    DownloadFileId downloadFileId();

    FilePersistenceType filePersistenceType();
}
