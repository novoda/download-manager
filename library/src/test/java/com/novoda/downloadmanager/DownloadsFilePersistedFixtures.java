package com.novoda.downloadmanager;

import static com.novoda.downloadmanager.DownloadBatchIdFixtures.aDownloadBatchId;

class DownloadsFilePersistedFixtures {
    private DownloadBatchId downloadBatchId = aDownloadBatchId().build();
    private FilePath filePath;
    private long totalFileSize;
    private String url;
    private DownloadFileId downloadFileId;

    private DownloadsFilePersistedFixtures() {
        // use aFile() to get an instance of this class
    }

    static DownloadsFilePersistedFixtures aDownloadsFilePersisted() {
        return new DownloadsFilePersistedFixtures();
    }

    DownloadsFilePersistedFixtures withDownloadBatchId(DownloadBatchId downloadBatchId) {
        this.downloadBatchId = downloadBatchId;
        return this;
    }

    DownloadsFilePersistedFixtures withFilePath(FilePath filePath) {
        this.filePath = filePath;
        return this;
    }

    DownloadsFilePersistedFixtures withTotalFileSize(long fileSize) {
        totalFileSize = fileSize;
        return this;
    }

    DownloadsFilePersistedFixtures withUrl(String url) {
        this.url = url;
        return this;
    }

    DownloadsFilePersistedFixtures withDownloadFileId(DownloadFileId downloadFileId) {
        this.downloadFileId = downloadFileId;
        return this;
    }

    DownloadsFilePersisted build() {
        return new DownloadsFilePersisted() {
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

        };
    }
}
