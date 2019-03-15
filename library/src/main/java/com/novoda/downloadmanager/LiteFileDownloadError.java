package com.novoda.downloadmanager;

class LiteFileDownloadError implements FileDownloader.FileDownloadError {

    private final String requestUrl;
    private final String message;
    private final int errorCode;

    LiteFileDownloadError(String requestUrl, String message, int errorCode) {
        this.requestUrl = requestUrl;
        this.message = message;
        this.errorCode = errorCode;
    }

    @Override
    public String requestUrl() {
        return requestUrl;
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
        if (requestUrl != null ? !requestUrl.equals(that.requestUrl) : that.requestUrl != null) {
            return false;
        }
        return message != null ? message.equals(that.message) : that.message == null;
    }

    @Override
    public int hashCode() {
        int result = requestUrl != null ? requestUrl.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + errorCode;
        return result;
    }

    @Override
    public String toString() {
        return "LiteFileDownloadError{"
                + "requestUrl='" + requestUrl + '\''
                + ", message='" + message + '\''
                + ", errorCode=" + errorCode
                + '}';
    }
}
