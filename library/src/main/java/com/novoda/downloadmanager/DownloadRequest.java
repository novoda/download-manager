package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.List;

public final class DownloadRequest {

    private final DownloadId id;
    private final ExternalId externalId;
    private final List<File> files;

    private DownloadRequest(DownloadId id, ExternalId externalId, List<File> files) {
        this.id = id;
        this.externalId = externalId;
        this.files = files;
    }

    public DownloadId getId() {
        return id;
    }

    public List<File> getFiles() {
        return files;
    }

    public ExternalId getExternalId() {
        return externalId;
    }

    public static final class File {

        private final String uri;
        private final String localUri;

        public File(String uri, String localUri) {
            this.uri = uri;
            this.localUri = localUri;
        }

        public String getUri() {
            return uri;
        }

        public String getLocalUri() {
            return localUri;
        }

        public static class Builder {

            private String uri;
            private String localUri;

            public Builder with(String uri) {
                this.uri = uri;
                return this;
            }

            public Builder withLocalUri(String localUri) {
                this.localUri = localUri;
                return this;
            }

            public File build() {
                return new File(uri, localUri);
            }

        }

    }

    public static class Builder {

        private List<File> files = new ArrayList<>();
        private DownloadId downloadId;
        private ExternalId externalId = ExternalId.NO_EXTERNAL_ID;

        public Builder with(DownloadId downloadId) {
            this.downloadId = downloadId;
            return this;
        }

        public Builder with(ExternalId externalId) {
            this.externalId = externalId;
            return this;
        }

        public Builder withFile(File file) {
            for (File existingFile : files) {
                if (existingFile.getLocalUri().equals(file.getLocalUri())) {
                    throw new IllegalArgumentException("File must not contain same local path as another file: " + existingFile.getLocalUri());
                }
            }
            files.add(file);
            return this;
        }

        public DownloadRequest build() {
            return new DownloadRequest(downloadId, externalId, files);
        }

    }

}
