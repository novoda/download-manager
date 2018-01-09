package com.novoda.downloadmanager;

class LiteDownloadsFilePersisted implements DownloadsFilePersisted {

    private final DownloadBatchId downloadBatchId;
    private final DownloadFileId downloadFileId;
    private final FileName fileName;
    private final FilePath filePath;
    private final long totalFileSize;
    private final String url;
    private final FilePersistenceType filePersistenceType;
    private final long downloadDateTimeInMillis;

    // We need to expose all of these.
    @SuppressWarnings({"checkstyle:parameternumber", "PMD.ExcessiveParameterList"})
    LiteDownloadsFilePersisted(DownloadBatchId downloadBatchId,
                               DownloadFileId downloadFileId,
                               FileName fileName,
                               FilePath filePath,
                               long totalFileSize,
                               String url,
                               FilePersistenceType filePersistenceType,
                               long downloadDateTimeInMillis) {
        this.downloadBatchId = downloadBatchId;
        this.downloadFileId = downloadFileId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.totalFileSize = totalFileSize;
        this.url = url;
        this.filePersistenceType = filePersistenceType;
        this.downloadDateTimeInMillis = downloadDateTimeInMillis;
    }

    @Override
    public DownloadBatchId downloadBatchId() {
        return downloadBatchId;
    }

    @Override
    public FileName fileName() {
        return fileName;
    }

    @Override
    public FilePath filePath() {
        return filePath;
    }

    @Override
    public long totalFileSize() {
        return totalFileSize;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public DownloadFileId downloadFileId() {
        return downloadFileId;
    }

    @Override
    public FilePersistenceType filePersistenceType() {
        return filePersistenceType;
    }

    @Override
    public long downloadDateTimeInMillis() {
        return downloadDateTimeInMillis;
    }
}
