package com.novoda.downloadmanager;

import java.io.IOException;
import java.io.InputStream;

interface HttpClient {

    DownloadManagerResponse execute(DownloadManagerRequest downloadManagerRequest) throws IOException;

    interface DownloadManagerResponse {

        int code();

        boolean isSuccessful();

        String header(String name, String defaultValue);

        InputStream openByteStream() throws IOException;

        void closeByteStream() throws IOException;
    }

}
