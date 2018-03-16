package com.novoda.downloadmanager;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

final class HttpClientFactory {

    private static final int TIMEOUT = 5;

    private HttpClientFactory() {
        // non-instantiable class
    }

    public static HttpClient getInstance() {
        return LazySingleton.INSTANCE;
    }

    private static class LazySingleton {

        private static final HttpClient INSTANCE = createInstance();

        private static HttpClient createInstance() {
            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.setConnectTimeout(TIMEOUT, TimeUnit.SECONDS);
            okHttpClient.setWriteTimeout(TIMEOUT, TimeUnit.SECONDS);
            okHttpClient.setReadTimeout(TIMEOUT, TimeUnit.SECONDS);
            return new WrappedOkHttpClient(okHttpClient);
        }
    }
}
