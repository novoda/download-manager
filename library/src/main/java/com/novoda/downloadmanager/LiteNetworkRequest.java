package com.novoda.downloadmanager;

import java.util.Map;

class LiteNetworkRequest implements NetworkRequest {

    private final Map<String, String> headers;
    private final String url;
    private final Method method;

    LiteNetworkRequest(Map<String, String> headers, String url, Method method) {
        this.headers = headers;
        this.url = url;
        this.method = method;
    }

    @Override
    public Map<String, String> headers() {
        return headers;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public Method method() {
        return method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LiteNetworkRequest that = (LiteNetworkRequest) o;

        if (headers != null ? !headers.equals(that.headers) : that.headers != null) {
            return false;
        }
        if (url != null ? !url.equals(that.url) : that.url != null) {
            return false;
        }
        return method == that.method;
    }

    @Override
    public int hashCode() {
        int result = headers != null ? headers.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (method != null ? method.hashCode() : 0);
        return result;
    }
}
