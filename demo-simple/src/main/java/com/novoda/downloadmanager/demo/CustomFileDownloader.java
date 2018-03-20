package com.novoda.downloadmanager.demo;

import android.util.Log;

import com.novoda.downloadmanager.FileDownloader;
import com.novoda.downloadmanager.FileSize;

class CustomFileDownloader implements FileDownloader {

    private static final String TAG = CustomFileDownloader.class.getSimpleName();
    private static final int BYTES_READ = 50;
    private static final int BUFFER_SIZE = 5000;
    private static final int SLEEP_IN_MILLIS = 200;

    private final byte[] buffer = new byte[BUFFER_SIZE];

    private boolean canDownload;

    @Override
    public void startDownloading(String url, FileSize fileSize, Callback callback) {
        Log.v(TAG, "Start downloading");

        canDownload = true;

        while (canDownload && fileSize.currentSize() < fileSize.totalSize()) {
            try {
                Thread.sleep(SLEEP_IN_MILLIS);
            } catch (InterruptedException e) {
                Log.e(TAG, "CustomFileDownloader Thread interrupted.", e);
            }
            callback.onBytesRead(buffer, BYTES_READ);
        }

        Log.v(TAG, "Download finished");
        callback.onDownloadFinished();
    }

    @Override
    public void stopDownloading() {
        Log.v(TAG, "Stop downloading");
        canDownload = false;
    }
}
