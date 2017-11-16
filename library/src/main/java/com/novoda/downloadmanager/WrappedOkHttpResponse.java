package com.novoda.downloadmanager;

import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;

public class WrappedOkHttpResponse implements HttpClient.DownloadManagerResponse {

    private final Response response;

    WrappedOkHttpResponse(Response response) {
        this.response = response;
    }

    @Override
    public InputStream openByteStream() throws IOException {
        return response.body().byteStream();
    }

    @Override
    public void closeByteStream() throws IOException {
        response.body().close();
    }
}
