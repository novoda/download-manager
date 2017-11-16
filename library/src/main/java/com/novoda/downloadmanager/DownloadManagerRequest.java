package com.novoda.downloadmanager;

import java.util.HashMap;
import java.util.Map;

class DownloadManagerRequest {

    private final Map<String, String> headers;
    private final String url;

    private DownloadManagerRequest(Map<String, String> headers, String url) {
        this.headers = headers;
        this.url = url;
    }

    Map<String, String> headers() {
        return headers;
    }

    String url() {
        return url;
    }

    class Builder {
        private static final String DOWNLOADED_BYTES_VALUE_FORMAT = "bytes=%s-%s";

        private String url;
        private HashMap<String, String> headers = new HashMap<>();

        Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        Builder withDownloadedBytesHeader(long currentSize, long totalSize) {
            String headerValue = String.format(DOWNLOADED_BYTES_VALUE_FORMAT, currentSize, totalSize);
            headers.put("Range", headerValue);
            return this;
        }

        DownloadManagerRequest build() {
            return new DownloadManagerRequest(headers, url);
        }
    }

}
