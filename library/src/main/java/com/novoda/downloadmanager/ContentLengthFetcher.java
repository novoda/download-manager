package com.novoda.downloadmanager;

import android.util.Pair;

import com.novoda.downloadmanager.domain.DownloadFile;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

class ContentLengthFetcher {

    private static final String HEADER_CONTENT_LENGTH = "Content-Length";
    private static final int UNKNOWN_CONTENT_LENGTH = -1;
    private static final int TIMEOUT_MILLIS = (int) TimeUnit.SECONDS.toMillis(10);

    private final OkHttpClient httpClient;

    public ContentLengthFetcher(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public long fetchContentLengthFor(DownloadFile file) {
        httpClient.setFollowRedirects(true);
        httpClient.setConnectTimeout(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        httpClient.setReadTimeout(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

        Request.Builder requestBuilder = new Request.Builder()
                .head()
                .url(file.getUri());

        appendHeaders(requestBuilder, file.getNetworkRequest());

        try {
            Response response = httpClient.newCall(requestBuilder.build()).execute();
            if (response.isSuccessful()) {
                return getHeaderFieldLong(response, HEADER_CONTENT_LENGTH, UNKNOWN_CONTENT_LENGTH);
            } else {
                return UNKNOWN_CONTENT_LENGTH;
            }
        } catch (IOException e) {
            // log exception
        }

        return UNKNOWN_CONTENT_LENGTH;
    }

    private void appendHeaders(Request.Builder requestBuilder, DownloadFile.NetworkRequest networkRequest) {
        for (Pair<String, String> header : networkRequest.getHeaders()) {
            requestBuilder.addHeader(header.first, header.second);
        }
    }

    private long getHeaderFieldLong(Response response, String field, long defaultValue) {
        return Long.parseLong(response.header(field, String.valueOf(defaultValue)));
    }

}