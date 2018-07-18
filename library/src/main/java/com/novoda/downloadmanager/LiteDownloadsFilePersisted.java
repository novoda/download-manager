package com.novoda.downloadmanager;

class LiteDownloadsFilePersisted implements DownloadsFilePersisted {

    private final DownloadBatchId downloadBatchId;
    private final DownloadFileId downloadFileId;
    private final FilePath filePath;
    private final long totalFileSize;
    private final String url;

    LiteDownloadsFilePersisted(DownloadBatchId downloadBatchId,
                               DownloadFileId downloadFileId,
                               FilePath filePath,
                               long totalFileSize,
                               String url) {
        this.downloadBatchId = downloadBatchId;
        this.downloadFileId = downloadFileId;
        this.filePath = filePath;
        this.totalFileSize = totalFileSize;
        this.url = url;
    }

    @Override
    public DownloadBatchId downloadBatchId() {
        return downloadBatchId;
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

}
