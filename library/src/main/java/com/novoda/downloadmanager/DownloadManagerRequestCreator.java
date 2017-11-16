package com.novoda.downloadmanager;

import java.util.HashMap;
import java.util.Map;

class DownloadManagerRequestCreator {

    private static final String DOWNLOADED_BYTES_VALUE_FORMAT = "bytes=%s-%s";

    DownloadManagerRequest createFileSizeRequest(String url) {
        return new DownloadManagerRequest(new HashMap<String, String>(), url, DownloadManagerRequest.Method.HEAD);
    }

    DownloadManagerRequest createDownloadRequest(String url) {
        return new DownloadManagerRequest(new HashMap<String, String>(), url, DownloadManagerRequest.Method.GET);
    }

    DownloadManagerRequest createDownloadRequestWithDownloadedBytesHeader(String url, long currentSize, long totalSize) {
        Map<String, String> headers = new HashMap<>();
        String headerValue = String.format(DOWNLOADED_BYTES_VALUE_FORMAT, currentSize, totalSize);
        headers.put("Range", headerValue);

        return new DownloadManagerRequest(headers, url, DownloadManagerRequest.Method.GET);
    }

}
