package com.novoda.downloadmanager;

public class NetworkErrorPayload {

    private final String request;
    private final String message;
    private final int errorCode;

    NetworkErrorPayload(String request, String message, int errorCode) {
        this.request = request;
        this.message = message;
        this.errorCode = errorCode;
    }

    public String request() {
        return request;
    }

    public String message() {
        return message;
    }

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

        NetworkErrorPayload that = (NetworkErrorPayload) o;

        if (errorCode != that.errorCode) {
            return false;
        }
        if (request != null ? !request.equals(that.request) : that.request != null) {
            return false;
        }
        return message != null ? message.equals(that.message) : that.message == null;
    }

    @Override
    public int hashCode() {
        int result = request != null ? request.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + errorCode;
        return result;
    }

    @Override
    public String toString() {
        return "NetworkErrorPayload{"
                + "request='" + request + '\''
                + ", message='" + message + '\''
                + ", errorCode=" + errorCode
                + '}';
    }
}
