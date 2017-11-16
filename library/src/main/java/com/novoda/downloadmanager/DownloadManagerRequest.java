package com.novoda.downloadmanager;

import java.util.Map;

class DownloadManagerRequest {

    private final Map<String, String> headers;
    private final String url;

    DownloadManagerRequest(Map<String, String> headers, String url) {
        this.headers = headers;
        this.url = url;
    }

    Map<String, String> headers() {
        return headers;
    }

    String url() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DownloadManagerRequest that = (DownloadManagerRequest) o;

        if (!headers.equals(that.headers)) {
            return false;
        }
        return url.equals(that.url);
    }

    @Override
    public int hashCode() {
        int result = headers.hashCode();
        result = 31 * result + url.hashCode();
        return result;
    }
}
