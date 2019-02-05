package com.novoda.downloadmanager;

import java.io.IOException;

class NetworkFileSizeHeaderRequest {

    private static final int ZERO_FILE_SIZE = 0;
    private static final String HEADER_CONTENT_LENGTH = "Content-Length";
    private static final int UNKNOWN_CONTENT_LENGTH = -1;

    private final HttpClient httpClient;
    private final NetworkRequestCreator requestCreator;
    private final NetworkFileSizeBodyRequest bodyRequest;

    NetworkFileSizeHeaderRequest(HttpClient httpClient, NetworkRequestCreator requestCreator, NetworkFileSizeBodyRequest bodyRequest) {
        this.httpClient = httpClient;
        this.requestCreator = requestCreator;
        this.bodyRequest = bodyRequest;
    }

    public Either<FileSize, DownloadError> requestFileSize(String url) {
        NetworkRequest fileSizeRequest = requestCreator.createFileSizeHeadRequest(url);
        NetworkResponse response = null;
        Either<FileSize, DownloadError> fileSizeOrError;
        try {
            response = httpClient.execute(fileSizeRequest);
            long headerResponseFileSize = processResponse(response);

            if (headerResponseFileSize == UNKNOWN_CONTENT_LENGTH || headerResponseFileSize == ZERO_FILE_SIZE) {
                Logger.w(String.format("file size header request '%s' returned %s, we'll try with a body request", url, headerResponseFileSize));
                fileSizeOrError = bodyRequest.requestFileSize(url);
            } else {
                fileSizeOrError = Either.asLeft(FileSizeCreator.createFromTotalSize(headerResponseFileSize));
            }

        } catch (IOException e) {
            return Either.asRight(DownloadErrorFactory.createTotalSizeRequestFailedError(e.getMessage()));
        } finally {
            if (response != null) {
                try {
                    response.closeByteStream();
                } catch (IOException e) {
                    Logger.e(e, "Error requesting file size for " + url);
                }
            }
        }
        return fileSizeOrError;
    }

    private long processResponse(NetworkResponse response) {
        long fileSize = ZERO_FILE_SIZE;
        if (response.isSuccessful()) {
            fileSize = Long.parseLong(response.header(HEADER_CONTENT_LENGTH, String.valueOf(UNKNOWN_CONTENT_LENGTH)));
            return fileSize;
        }
        return fileSize;
    }
}
