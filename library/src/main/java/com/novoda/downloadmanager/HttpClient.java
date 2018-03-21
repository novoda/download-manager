package com.novoda.downloadmanager;

import java.io.IOException;
import java.io.InputStream;

interface HttpClient {

    NetworkResponse execute(NetworkRequest networkRequest) throws IOException;

    interface NetworkResponse {

        int code();

        boolean isSuccessful();

        String header(String name, String defaultValue);

        InputStream openByteStream() throws IOException;

        void closeByteStream() throws IOException;

        long bodyContentLength();
    }
}
