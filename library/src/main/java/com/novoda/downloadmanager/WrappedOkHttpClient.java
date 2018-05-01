package com.novoda.downloadmanager;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

class WrappedOkHttpClient implements HttpClient {

    private final OkHttpClient httpClient;

    WrappedOkHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public NetworkResponse execute(NetworkRequest request) throws IOException {
        Request.Builder requestBuilder = new Request.Builder()
                .url(request.url());

        if (request.method() == NetworkRequest.Method.HEAD) {
            requestBuilder = requestBuilder.head();
        }

        for (Map.Entry<String, String> entry : request.headers().entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }

        Call call = httpClient.newCall(requestBuilder.build());

        return new WrappedOkHttpResponse(call.execute());
    }
}
