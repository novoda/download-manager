package com.novoda.downloadmanager.domain;

import java.util.ArrayList;
import java.util.List;

public class DownloadRequest {

    private final DownloadId id;
    private final String identifier;
    private final List<File> files;

    DownloadRequest(DownloadId id, String identifier, List<File> files) {
        this.id = id;
        this.identifier = identifier;
        this.files = files;
    }

    public DownloadId getId() {
        return id;
    }

    public List<File> getFiles() {
        return files;
    }

    public String getIdentifier() {
        return identifier;
    }

    public static class File {

        private final String uri;
        private final String localUri;
        private final String identifier;

        public File(String uri, String localUri, String identifier) {
            this.uri = uri;
            this.localUri = localUri;
            this.identifier = identifier;
        }

        public String getUri() {
            return uri;
        }

        public String getIdentifier() {
            return identifier;
        }

        public String getLocalUri() {
            return localUri;
        }

        public static class Builder {

            private final DownloadRequest.Builder requestBuilder;

            private String identifier;
            private String uri;
            private String localUri;

            public Builder(DownloadRequest.Builder requestBuilder) {
                this.requestBuilder = requestBuilder;
            }

            public Builder with(String uri) {
                this.uri = uri;
                return this;
            }

            public Builder withIdentifier(String identifier) {
                this.identifier = identifier;
                return this;
            }

            public Builder withLocalUri(String localUri) {
                this.localUri = localUri;
                return this;
            }

            public DownloadRequest.Builder build() {
                requestBuilder.withFile(new File(uri, identifier, localUri));
                return requestBuilder;
            }

        }

    }

    public static class Builder {

        private List<File> files = new ArrayList<>();
        private DownloadId downloadId;
        private String identifier;

        public Builder with(DownloadId downloadId) {
            this.downloadId = downloadId;
            return this;
        }

        public Builder with(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder withFile(File file) {
            files.add(file);
            return this;
        }

        public DownloadRequest build() {
            return new DownloadRequest(downloadId, identifier == null ? downloadId.toString() : identifier, files);
        }

    }

}
