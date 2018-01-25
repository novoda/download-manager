package com.novoda.downloadmanager;

import java.util.HashMap;
import java.util.Map;

public final class Batch {

    private final DownloadBatchId downloadBatchId;
    private final String title;
    private final Map<DownloadFileId, NetworkAddressAndFileName> networkAddressAndFileNameById;

    Batch(DownloadBatchId downloadBatchId, String title, Map<DownloadFileId, NetworkAddressAndFileName> networkAddressAndFileNameById) {
        this.downloadBatchId = downloadBatchId;
        this.title = title;
        this.networkAddressAndFileNameById = networkAddressAndFileNameById;
    }

    DownloadBatchId getDownloadBatchId() {
        return downloadBatchId;
    }

    String getTitle() {
        return title;
    }

    Map<DownloadFileId, NetworkAddressAndFileName> networkAddressAndFileNameById() {
        return new HashMap<>(networkAddressAndFileNameById);
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
        return networkAddressAndFileNameById != null
                ? networkAddressAndFileNameById.equals(batch.networkAddressAndFileNameById) : batch.networkAddressAndFileNameById == null;
    }

    @Override
    public int hashCode() {
        int result = downloadBatchId != null ? downloadBatchId.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (networkAddressAndFileNameById != null ? networkAddressAndFileNameById.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Batch{"
                + "downloadBatchId=" + downloadBatchId
                + ", title='" + title + '\''
                + ", networkAddressAndFileNameById=" + networkAddressAndFileNameById
                + '}';
    }

    public static class Builder {

        private final DownloadBatchId downloadBatchId;
        private final String title;
        private final Map<DownloadFileId, NetworkAddressAndFileName> networkAddressAndFileNameById = new HashMap<>();

        public Builder(DownloadBatchId downloadBatchId, String title) {
            this.downloadBatchId = downloadBatchId;
            this.title = title;
        }

        public Builder addFile(String fileUrl) {
            String rawId = downloadBatchId.rawId() + fileUrl;
            DownloadFileId downloadFileId = DownloadFileIdCreator.createFrom(rawId);
            FileName fileName = FileNameExtractor.extractFrom(fileUrl);
            NetworkAddressAndFileName networkAddressAndFileName = new NetworkAddressAndFileName(fileUrl, fileName);
            networkAddressAndFileNameById.put(downloadFileId, networkAddressAndFileName);
            return this;
        }

        public Builder addFile(DownloadFileId downloadFileId, String fileUrl) {
            FileName fileName = FileNameExtractor.extractFrom(fileUrl);
            NetworkAddressAndFileName networkAddressAndFileName = new NetworkAddressAndFileName(fileUrl, fileName);
            networkAddressAndFileNameById.put(downloadFileId, networkAddressAndFileName);
            return this;
        }

        public Builder addFile(DownloadFileId downloadFileId, String fileUrl, String fileName) {
            NetworkAddressAndFileName networkAddressAndFileName = new NetworkAddressAndFileName(fileUrl, LiteFileName.from(fileName));
            networkAddressAndFileNameById.put(downloadFileId, networkAddressAndFileName);
            return this;
        }

        public Batch build() {
            return new Batch(downloadBatchId, title, networkAddressAndFileNameById);
        }
    }

}
