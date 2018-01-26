package com.novoda.downloadmanager;

import java.util.HashMap;
import java.util.Map;

public final class Batch {

    private final DownloadBatchId downloadBatchId;
    private final String title;
    private final Map<DownloadFileId, NetworkAddressAndFilePath> networkAddressAndFilePathById;

    Batch(DownloadBatchId downloadBatchId, String title, Map<DownloadFileId, NetworkAddressAndFilePath> networkAddressAndFilePathById) {
        this.downloadBatchId = downloadBatchId;
        this.title = title;
        this.networkAddressAndFilePathById = networkAddressAndFilePathById;
    }

    DownloadBatchId getDownloadBatchId() {
        return downloadBatchId;
    }

    String getTitle() {
        return title;
    }

    Map<DownloadFileId, NetworkAddressAndFilePath> networkAddressAndFileNameById() {
        return new HashMap<>(networkAddressAndFilePathById);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Batch batch = (Batch) o;

        if (downloadBatchId != null ? !downloadBatchId.equals(batch.downloadBatchId) : batch.downloadBatchId != null) {
            return false;
        }
        if (title != null ? !title.equals(batch.title) : batch.title != null) {
            return false;
        }
        return networkAddressAndFilePathById != null
                ? networkAddressAndFilePathById.equals(batch.networkAddressAndFilePathById) : batch.networkAddressAndFilePathById == null;
    }

    @Override
    public int hashCode() {
        int result = downloadBatchId != null ? downloadBatchId.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (networkAddressAndFilePathById != null ? networkAddressAndFilePathById.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Batch{"
                + "downloadBatchId=" + downloadBatchId
                + ", title='" + title + '\''
                + ", networkAddressAndFilePathById=" + networkAddressAndFilePathById
                + '}';
    }

    public static class Builder {

        private final DownloadBatchId downloadBatchId;
        private final String title;
        private final Map<DownloadFileId, NetworkAddressAndFilePath> networkAddressAndFilePathById = new HashMap<>();

        public Builder(DownloadBatchId downloadBatchId, String title) {
            this.downloadBatchId = downloadBatchId;
            this.title = title;
        }

        public Builder addFile(String fileUrl) {
            String rawId = downloadBatchId.rawId() + fileUrl;
            DownloadFileId downloadFileId = DownloadFileIdCreator.createFrom(rawId);
            return addFile(downloadFileId, fileUrl);
        }

        public Builder addFile(DownloadFileId downloadFileId, String fileUrl) {
            return addFile(downloadFileId, fileUrl, FilePathCreator.unknownFilePath());
        }

        public Builder addFile(DownloadFileId downloadFileId, String fileUrl, FilePath filePath) {
            NetworkAddressAndFilePath networkAddressAndFilePath = new NetworkAddressAndFilePath(fileUrl, filePath);
            networkAddressAndFilePathById.put(downloadFileId, networkAddressAndFilePath);
            return this;
        }

        public Batch build() {
            return new Batch(downloadBatchId, title, networkAddressAndFilePathById);
        }
    }

}
