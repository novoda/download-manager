package com.novoda.downloadmanager.demo;

import com.novoda.downloadmanager.FileDownloader;
import com.novoda.downloadmanager.FileSize;
import com.novoda.notils.logger.simple.Log;

class CustomFileDownloader implements FileDownloader {

    private static final int BYTES_READ = 50;
    private static final int BUFFER_SIZE = 5000;
    private static final int SLEEP_IN_MILLIS = 200;

    private byte[] buffer = new byte[BUFFER_SIZE];

    private boolean canDownload;

    @Override
    public void startDownloading(String url, FileSize fileSize, Callback callback) {
        Log.v("Start downloading");

        canDownload = true;

        while (canDownload && fileSize.currentSize() < fileSize.totalSize()) {
            try {
                Thread.sleep(SLEEP_IN_MILLIS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            callback.onBytesRead(buffer, BYTES_READ);
        }

        Log.v("Download finished");
        callback.onDownloadFinished();
    }

    @Override
    public void stopDownloading() {
        Log.v("Stop downloading");
        canDownload = false;
    }
}
