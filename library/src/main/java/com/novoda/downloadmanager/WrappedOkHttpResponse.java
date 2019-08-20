package com.novoda.downloadmanager;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Response;
import okhttp3.ResponseBody;

class WrappedOkHttpResponse implements NetworkResponse {

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
        ResponseBody body = response.body();
        if (body == null) {
            throw new IOException("Response body is null");
        } else {
            return body.byteStream();
        }
    }

    @Override
    public void closeByteStream() throws IOException {
        ResponseBody body = response.body();
        if (body == null) {
            throw new IOException("Response body is null");
        } else {
            body.close();
        }
    }

    @Override
    public long bodyContentLength() {
        ResponseBody body = response.body();
        if (body == null) {
            return -1;
        } else {
            return body.contentLength();
        }
    }
}
