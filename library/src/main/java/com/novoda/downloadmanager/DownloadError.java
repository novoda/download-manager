package com.novoda.downloadmanager;

public class DownloadError {

    public enum Type {
        FILE_CURRENT_AND_TOTAL_SIZE_MISMATCH,
        FILE_TOTAL_SIZE_REQUEST_FAILED,
        FILE_CANNOT_BE_CREATED_LOCALLY_INSUFFICIENT_FREE_SPACE,
        FILE_CANNOT_BE_WRITTEN,
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DownloadError that = (DownloadError) o;

        if (type != that.type) {
            return false;
        }
        return message != null ? message.equals(that.message) : that.message == null;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DownloadError{"
                + "type=" + type
                + ", message='" + message + '\''
                + '}';
    }
}
