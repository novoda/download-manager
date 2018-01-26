package com.novoda.downloadmanager;

import android.support.annotation.Nullable;

class FileBuilder {

    private final DownloadBatchId downloadBatchId;
    private final String networkAddress;

    @Nullable
    private DownloadFileId downloadFileId;
    @Nullable
    private FilePath relativePathToStoreDownload;

    FileBuilder(DownloadBatchId downloadBatchId, String networkAddress) {
        this.downloadBatchId = downloadBatchId;
        this.networkAddress = networkAddress;
    }

    FileBuilder withFileIdentifier(DownloadFileId downloadFileId) {
        this.downloadFileId = downloadFileId;
        return this;
    }

    FileBuilder withRelativePathToStoreDownload(FilePath relativePathToStoreDownload) {
        this.relativePathToStoreDownload = relativePathToStoreDownload;
        return this;
    }

    DownloadIdNetworkAddressAndFilePath done() {
        return new Batch.BatchBuilder()
        return new DownloadIdNetworkAddressAndFilePath(downloadFileId, networkAddress, relativePathToStoreDownload);
    }

    class DownloadIdNetworkAddressAndFilePath {
        private DownloadFileId downloadFileId;
        private String networkAddress;
        private FilePath filePath;

        public DownloadIdNetworkAddressAndFilePath(DownloadFileId downloadFileId, String networkAddress, FilePath filePath) {
            this.downloadFileId = downloadFileId;
            this.networkAddress = networkAddress;
            this.filePath = filePath;
        }

        public DownloadFileId downloadFileId() {
            return downloadFileId;
        }

        public String networkAddress() {
            return networkAddress;
        }

        public FilePath filePath() {
            return filePath;
        }
    }

}
