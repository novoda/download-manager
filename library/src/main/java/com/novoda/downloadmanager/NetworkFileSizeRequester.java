package com.novoda.downloadmanager;

import java.io.IOException;

class NetworkFileSizeRequester implements FileSizeRequester {

    private static final String HEADER_CONTENT_LENGTH = "Content-Length";
    private static final int UNKNOWN_CONTENT_LENGTH = -1;
    private static final int ZERO_FILE_SIZE = 0;

    private final HttpClient httpClient;
    private final NetworkRequestCreator requestCreator;

    NetworkFileSizeRequester(HttpClient httpClient, NetworkRequestCreator requestCreator) {
        this.httpClient = httpClient;
        this.requestCreator = requestCreator;
    }

    @Override
    public FileSize requestFileSize(String url) {
        try {
            long fileSize = executeRequestFileSize(url);
            if (fileSize == 0) {
                return FileSizeCreator.unknownFileSize();
            } else {
                return FileSizeCreator.createFromTotalSize(fileSize);
            }
        } catch (IOException e) {
            Logger.e(e, "Error requesting file size for " + url);
        }

        return FileSizeCreator.unknownFileSize();
    }

    private long executeRequestFileSize(String url) throws IOException {
        long fileSize = requestFileSizeThroughHeaderRequest(url);
        if (fileSize == 0) {
            Logger.w("filesize request through header returned zero, we'll try again " + url);
            fileSize = requestFileSizeThroughBodyRequest(url);
            if (fileSize == 0) {
                Logger.w("filesize request through body returned zero");
            }
        }

        return fileSize;
    }

    private long requestFileSizeThroughHeaderRequest(String url) throws IOException {
        NetworkRequest fileSizeRequest = requestCreator.createFileSizeRequest(url);
        HttpClient.NetworkResponse response = httpClient.execute(fileSizeRequest);
        long fileSize = ZERO_FILE_SIZE;
        if (response.isSuccessful()) {
            fileSize = Long.parseLong(response.header(HEADER_CONTENT_LENGTH, String.valueOf(UNKNOWN_CONTENT_LENGTH)));
            response.closeByteStream();
        }
        return fileSize;
    }

    private long requestFileSizeThroughBodyRequest(String url) throws IOException {
        NetworkRequest downloadRequest = requestCreator.createDownloadRequest(url);
        HttpClient.NetworkResponse response = httpClient.execute(downloadRequest);
        long fileSize = ZERO_FILE_SIZE;
        if (response.isSuccessful()) {
            fileSize = response.bodyContentLength();
            response.closeByteStream();
        }

        return fileSize;
    }
}
