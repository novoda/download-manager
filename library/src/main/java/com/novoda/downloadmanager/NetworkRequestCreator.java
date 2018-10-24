package com.novoda.downloadmanager;

import java.util.HashMap;
import java.util.Map;

class NetworkRequestCreator {

    private static final String DOWNLOADED_BYTES_VALUE_FORMAT = "bytes=%s-%s";
    private static final Map<String, String> DISABLE_COMPRESSION_HEADERS = new HashMap<>(1);

    static {
        DISABLE_COMPRESSION_HEADERS.put("Accept-Encoding", "identity");
    }

    NetworkRequest createFileSizeHeadRequest(String url) {
        return new LiteNetworkRequest(DISABLE_COMPRESSION_HEADERS, url, NetworkRequest.Method.HEAD);
    }

    NetworkRequest createFileSizeBodyRequest(String url) {
        return new LiteNetworkRequest(DISABLE_COMPRESSION_HEADERS, url, NetworkRequest.Method.GET);
    }

    NetworkRequest createDownloadRequest(String url) {
        return new LiteNetworkRequest(new HashMap<>(), url, NetworkRequest.Method.GET);
    }

    NetworkRequest createDownloadRequestWithDownloadedBytesHeader(String url, long currentSize, long totalSize) {
        Map<String, String> headers = new HashMap<>();
        String headerValue = String.format(DOWNLOADED_BYTES_VALUE_FORMAT, currentSize, totalSize - 1);
        headers.put("Range", headerValue);

        return new LiteNetworkRequest(headers, url, NetworkRequest.Method.GET);
    }
}
