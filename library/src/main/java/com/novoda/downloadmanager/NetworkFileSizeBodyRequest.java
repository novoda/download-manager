package com.novoda.downloadmanager;

import java.io.IOException;

class NetworkFileSizeBodyRequest {

    private static final int ZERO_FILE_SIZE = 0;
    private static final int UNKNOWN_CONTENT_LENGTH = -1;

    private final HttpClient httpClient;
    private final NetworkRequestCreator requestCreator;

    NetworkFileSizeBodyRequest(HttpClient httpClient, NetworkRequestCreator requestCreator) {
        this.httpClient = httpClient;
        this.requestCreator = requestCreator;
    }

    public Either<FileSize, DownloadError> requestFileSize(String url) {
        NetworkRequest fileSizeRequest = requestCreator.createFileSizeBodyRequest(url);
        NetworkResponse response = null;
        Either<FileSize, DownloadError> fileSizeOrError;
        try {
            response = httpClient.execute(fileSizeRequest);
            fileSizeOrError = processResponse(response, url);
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

    private Either<FileSize, DownloadError> processResponse(NetworkResponse response, String url) {
        if (response.isSuccessful()) {
            long fileSize = response.bodyContentLength();
            if (fileSize == UNKNOWN_CONTENT_LENGTH || fileSize == ZERO_FILE_SIZE) {
                String errorMessage = String.format(
                        "File Size Body Request: '%s' returned '%s'",
                        url,
                        fileSize
                );
                return Either.asRight(DownloadErrorFactory.createTotalSizeRequestFailedError(errorMessage));
            }

            return Either.asLeft(FileSizeCreator.createFromTotalSize(fileSize));
        } else {
            Logger.w(String.format("file size body request '%s'", url));
            String errorMessage = String.format(
                    "File Size Body Request: '%s' with response code: '%s' failed.",
                    url,
                    response.code()
            );
            return Either.asRight(DownloadErrorFactory.createTotalSizeRequestFailedError(errorMessage));
        }
    }
}
