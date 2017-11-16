package com.novoda.downloadmanager;

import java.util.Map;

class NetworkRequest {

    private final Map<String, String> headers;
    private final String url;
    private final Method method;

    NetworkRequest(Map<String, String> headers, String url, Method method) {
        this.headers = headers;
        this.url = url;
        this.method = method;
    }

    Map<String, String> headers() {
        return headers;
    }

    String url() {
        return url;
    }

    Method method() {
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

        NetworkRequest that = (NetworkRequest) o;

        if (!headers.equals(that.headers)) {
            return false;
        }
        if (!url.equals(that.url)) {
            return false;
        }
        return method == that.method;
    }

    @Override
    public int hashCode() {
        int result = headers.hashCode();
        result = 31 * result + url.hashCode();
        result = 31 * result + method.hashCode();
        return result;
    }

    enum Method {
        GET("get"),
        HEAD("head");

        private final String rawMethod;

        Method(String rawMethod) {
            this.rawMethod = rawMethod;
        }

        String rawMethod() {
            return rawMethod;
        }
    }
}
