package com.novoda.downloadmanager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

class NetworkResponseFixtures {

    private int code = 200;
    private boolean isSuccessful = true;
    private String header = "header";
    private InputStream inputStream = new ByteArrayInputStream("input".getBytes());
    private long bodyContentLength = 0;

    static NetworkResponseFixtures aNetworkResponse() {
        return new NetworkResponseFixtures();
    }

    NetworkResponseFixtures withCode(int code) {
        this.code = code;
        return this;
    }

    NetworkResponseFixtures withSuccessful(boolean successful) {
        isSuccessful = successful;
        return this;
    }

    NetworkResponseFixtures withHeader(String header) {
        this.header = header;
        return this;
    }

    NetworkResponseFixtures withInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
        return this;
    }

    NetworkResponseFixtures withBodyContentLength(long bodyContentLength) {
        this.bodyContentLength = bodyContentLength;
        return this;
    }


    HttpClient.NetworkResponse build() {
        return new HttpClient.NetworkResponse() {
            @Override
            public int code() {
                return code;
            }

            @Override
            public boolean isSuccessful() {
                return isSuccessful;
            }

            @Override
            public String header(String name, String defaultValue) {
                return header;
            }

            @Override
            public InputStream openByteStream() throws IOException {
                return inputStream;
            }

            @Override
            public void closeByteStream() throws IOException {
                inputStream.close();
            }

            @Override
            public long bodyContentLength() {
                return bodyContentLength;
            }
        };
    }
}
