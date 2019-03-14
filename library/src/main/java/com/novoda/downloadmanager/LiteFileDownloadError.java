package com.novoda.downloadmanager;

class LiteFileDownloadError implements FileDownloader.FileDownloadError {

    private final String rawRequest;
    private final String message;
    private final int errorCode;

    LiteFileDownloadError(String rawRequest, String message, int errorCode) {
        this.rawRequest = rawRequest;
        this.message = message;
        this.errorCode = errorCode;
    }

    @Override
    public String rawRequest() {
        return rawRequest;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public int errorCode() {
        return errorCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LiteFileDownloadError that = (LiteFileDownloadError) o;

        if (errorCode != that.errorCode) {
            return false;
        }
        if (rawRequest != null ? !rawRequest.equals(that.rawRequest) : that.rawRequest != null) {
            return false;
        }
        return message != null ? message.equals(that.message) : that.message == null;
    }

    @Override
    public int hashCode() {
        int result = rawRequest != null ? rawRequest.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + errorCode;
        return result;
    }

    @Override
    public String toString() {
        return "LiteFileDownloadError{"
                + "rawRequest='" + rawRequest + '\''
                + ", message='" + message + '\''
                + ", errorCode=" + errorCode
                + '}';
    }
}
