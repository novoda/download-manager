package com.novoda.downloadmanager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

final class LiteHttpClientFactory implements HttpClientFactory {

    private static final int TIMEOUT = 5;

    @Override
    public HttpClient create() {
        return LazySingleton.INSTANCE;
    }

    private static class LazySingleton {

        private static final HttpClient INSTANCE = createInstance();

        private static HttpClient createInstance() {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .build();
            return new WrappedOkHttpClient(okHttpClient);
        }
    }
}
