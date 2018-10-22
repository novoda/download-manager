package com.novoda.downloadmanager;

import java.util.Hashtable;
import java.util.Map;

class NetworkRequestFixtures {

    private Map<String, String> headers = new Hashtable<>();
    private String url = "http://www.google.com";
    private NetworkRequest.Method method = NetworkRequest.Method.GET;

    static NetworkRequestFixtures aNetworkRequest() {
        return new NetworkRequestFixtures();
    }

    NetworkRequestFixtures withHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    NetworkRequestFixtures withUrl(String url) {
        this.url = url;
        return this;
    }

    NetworkRequestFixtures withMethod(NetworkRequest.Method method) {
        this.method = method;
        return this;
    }

    NetworkRequest build() {
        return new StubNetworkRequest(headers, url, method);
    }

    private static class StubNetworkRequest implements NetworkRequest {

        private final Map<String, String> headers;
        private final String url;
        private final Method method;

        private StubNetworkRequest(Map<String, String> headers, String url, Method method) {
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
    }
}
