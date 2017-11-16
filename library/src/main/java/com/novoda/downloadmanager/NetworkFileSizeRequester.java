package com.novoda.downloadmanager;

import com.novoda.notils.logger.simple.Log;

import java.io.IOException;

class NetworkFileSizeRequester implements FileSizeRequester {

    private static final String HEADER_CONTENT_LENGTH = "Content-Length";
    private static final int UNKNOWN_CONTENT_LENGTH = -1;

    private final HttpClient httpClient;
    private final DownloadManagerRequestCreator requestCreator;

    NetworkFileSizeRequester(HttpClient httpClient, DownloadManagerRequestCreator requestCreator) {
        this.httpClient = httpClient;
        this.requestCreator = requestCreator;
    }

    @Override
    public FileSize requestFileSize(String url) {
        DownloadManagerRequest fileSizeRequest = requestCreator.createFileSizeRequest(url);

        try {
            HttpClient.DownloadManagerResponse response = httpClient.execute(fileSizeRequest);
            if (response.isSuccessful()) {
                long totalFileSize = Long.parseLong(response.header(HEADER_CONTENT_LENGTH, String.valueOf(UNKNOWN_CONTENT_LENGTH)));
                return FileSizeCreator.createFromTotalSize(totalFileSize);
            }
        } catch (IOException e) {
            Log.e(e, "Error requesting file size for " + url);
        }

        return FileSizeCreator.UNKNOWN;
    }
}