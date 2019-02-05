package com.novoda.downloadmanager;

import java.io.IOException;

class NetworkFileSizeBodyRequest implements FileSizeRequester {

    private static final int ZERO_FILE_SIZE = 0;
    private static final int UNKNOWN_CONTENT_LENGTH = -1;

    private final HttpClient httpClient;
    private final NetworkRequestCreator requestCreator;

    NetworkFileSizeBodyRequest(HttpClient httpClient, NetworkRequestCreator requestCreator) {
        this.httpClient = httpClient;
        this.requestCreator = requestCreator;
    }

    @Override
    public FileSize requestFileSize(String url) {
        return null;
    }

    @Override
    public FileSizeResult requestFileSizeResult(String url) {
        NetworkRequest fileSizeRequest = requestCreator.createFileSizeBodyRequest(url);
        NetworkResponse response = null;
        FileSizeResult fileSizeOrError;
        try {
            response = httpClient.execute(fileSizeRequest);
            fileSizeOrError = processResponse(response, url);
        } catch (IOException e) {
            return FileSizeResult.failure(e.getMessage());
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

    private FileSizeResult processResponse(NetworkResponse response, String url) {
        if (response.isSuccessful()) {
            long fileSize = response.bodyContentLength();
            if (fileSize == UNKNOWN_CONTENT_LENGTH || fileSize == ZERO_FILE_SIZE) {
                String errorMessage = String.format(
                        "File Size Body Request: '%s' returned '%s'",
                        url,
                        fileSize
                );
                return FileSizeResult.failure(errorMessage);
            }

            return FileSizeResult.success(FileSizeCreator.createFromTotalSize(fileSize));
        } else {
            Logger.w(String.format("file size body request '%s'", url));
            String errorMessage = String.format(
                    "File Size Body Request: '%s' with response code: '%s' failed.",
                    url,
                    response.code()
            );
            return FileSizeResult.failure(errorMessage);
        }
    }
}
