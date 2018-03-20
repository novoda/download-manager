package com.novoda.downloadmanager;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Response;

class WrappedOkHttpResponse implements HttpClient.NetworkResponse {

    private final Response response;

    WrappedOkHttpResponse(Response response) {
        this.response = response;
    }

    @Override
    public int code() {
        return response.code();
    }

    @Override
    public boolean isSuccessful() {
        return response.isSuccessful();
    }

    @Override
    public String header(String name, String defaultValue) {
        return response.header(name, defaultValue);
    }

    @Override
    public InputStream openByteStream() throws IOException {
        return response.body().byteStream();
    }

    @Override
    public void closeByteStream() throws IOException {
        response.body().close();
    }

    @Override
    public long bodyContentLength() {
        return response.body().contentLength();
    }
}
