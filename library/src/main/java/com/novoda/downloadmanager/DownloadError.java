package com.novoda.downloadmanager;

public class DownloadError {

    public enum Error {
        FILE_TOTAL_SIZE_REQUEST_FAILED,
        FILE_CANNOT_BE_CREATED_LOCALLY_INSUFFICIENT_FREE_SPACE,
        FILE_CANNOT_BE_WRITTEN,
        STORAGE_UNAVAILABLE,
        NETWORK_ERROR_CANNOT_DOWNLOAD_FILE,
        UNKNOWN
    }

    private final Error error;

    DownloadError(Error error) {
        this.error = error;
    }

    Error error() {
        return error;
    }
}
