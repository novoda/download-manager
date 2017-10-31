package com.novoda.downloadmanager;

import com.novoda.notils.logger.simple.Log;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

class NetworkFileDownloader implements FileDownloader {

    private static final int BUFFER_SIZE = 8 * 512;

    private final OkHttpClient httpClient;

    private boolean canDownload;

    NetworkFileDownloader(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void startDownloading(String url, FileSize fileSize, Callback callback) {
        canDownload = true;

        Request.Builder requestBuilder = new Request.Builder().url(url);
        if (fileSize.areBytesDownloadedKnown()) {
            requestBuilder.addHeader("Range", "bytes=" + fileSize.currentSize() + "-" + fileSize.totalSize());
        }

        Call call = httpClient.newCall(requestBuilder.build());
        Response response = null;
        try {
            response = call.execute();
            int responseCode = response.code();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_PARTIAL) {
                InputStream in = response.body().byteStream();
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
                    response.body().close();
                }
            } catch (IOException e) {
                Log.e(e, "Exception while closing the body response");
            }
        }

        callback.onDownloadFinished();
    }

    @Override
    public void stopDownloading() {
        canDownload = false;
    }
}
