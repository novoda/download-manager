package com.novoda.downloadmanager;

import java.io.IOException;
import java.io.InputStream;

interface HttpClient {

    DownloadManagerResponse execute(DownloadManagerRequest downloadManagerRequest) throws IOException;

    interface DownloadManagerResponse {

        InputStream openByteStream() throws IOException;

        void closeByteStream() throws IOException;
    }

}
