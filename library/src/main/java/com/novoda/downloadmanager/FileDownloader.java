package com.novoda.downloadmanager;

/**
 * For defining the mechanism by which files are downloaded.
 */
public interface FileDownloader {

    void startDownloading(String url, FileSize fileSize, Callback callback);

    void stopDownloading();

    interface Callback {

        void onBytesRead(byte[] buffer, int bytesRead);

        void onError(String cause);

        void onDownloadFinished();
    }
}
