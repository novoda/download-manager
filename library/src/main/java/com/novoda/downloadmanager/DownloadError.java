package com.novoda.downloadmanager;

public class DownloadError {

    public enum Type {
        FILE_CURRENT_AND_TOTAL_SIZE_MISMATCH,
        FILE_TOTAL_SIZE_REQUEST_FAILED,
        FILE_CANNOT_BE_CREATED_LOCALLY_INSUFFICIENT_FREE_SPACE,
        FILE_CANNOT_BE_WRITTEN,
        STORAGE_UNAVAILABLE,
        NETWORK_ERROR_CANNOT_DOWNLOAD_FILE,
        UNKNOWN
    }

    private final Type type;
    private final String message;

    DownloadError(Type type, String message) {
        this.type = type;
        this.message = message;
    }

    DownloadError(Type type) {
        this.type = type;
        this.message = "";
    }

    public Type type() {
        return type;
    }

    public String message() {
        return message;
    }
}
