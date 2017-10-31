package com.novoda.downloadmanager;

public interface FileDownloader {

    void startDownloading(String url, FileSize fileSize, Callback callback);

    void stopDownloading();

    interface Callback {

        void onBytesRead(byte[] buffer, int bytesRead);

        void onError();

        void onDownloadFinished();
    }
}
