package com.novoda.downloadmanager;

import com.novoda.notils.logger.simple.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

class NetworkFileDownloader implements FileDownloader {

    private static final int BUFFER_SIZE = 8 * 512;

    private final HttpClient httpClient;
    private final NetworkRequestCreator requestCreator;

    private boolean canDownload;

    NetworkFileDownloader(HttpClient httpClient, NetworkRequestCreator requestCreator) {
        this.httpClient = httpClient;
        this.requestCreator = requestCreator;
    }

    @Override
    public void startDownloading(String url, FileSize fileSize, Callback callback) {
        canDownload = true;

        NetworkRequest request = createRequestFrom(url, fileSize);
        HttpClient.NetworkResponse response = null;
        try {
            response = httpClient.execute(request);
            int responseCode = response.code();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_PARTIAL) {
                InputStream in = response.openByteStream();
                byte[] buffer = new byte[BUFFER_SIZE];
                int readLast = 0;
                while (canDownload && readLast != -1) {
                    readLast = in.read(buffer);

                    if (readLast != 0 && readLast != -1) {
                        callback.onBytesRead(buffer, readLast);
                    }
                }
            } else {
                Log.e("Network response code is not ok, responseCode: " + responseCode);
                callback.onError();
            }
        } catch (IOException e) {
            Log.e(e, "Exception with http request");
            callback.onError();
        } finally {
            try {
                if (response != null) {
                    response.closeByteStream();
                }
            } catch (IOException e) {
                Log.e(e, "Exception while closing the body response");
            }
        }

        callback.onDownloadFinished();
    }

    private NetworkRequest createRequestFrom(String url, FileSize fileSize) {
        if (fileSize.areBytesDownloadedKnown()) {
            return requestCreator.createDownloadRequestWithDownloadedBytesHeader(url, fileSize.currentSize(), fileSize.totalSize());
        } else {
            return requestCreator.createDownloadRequest(url);
        }
    }

    @Override
    public void stopDownloading() {
        canDownload = false;
    }
}
