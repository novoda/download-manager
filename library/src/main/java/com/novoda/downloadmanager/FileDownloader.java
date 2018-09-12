package com.novoda.downloadmanager;

/**
 * For defining the mechanism by which files are downloaded.
 */
public interface FileDownloader {

    /**
     * Called internally to start downloading a file.
     *
     * @param url      of the asset to download.
     * @param fileSize the byte ranges, represented as file sizes, used to download an asset.
     * @param callback that is notified of download progress.
     */
    void startDownloading(String url, FileSize fileSize, Callback callback);

    /**
     * Called internally to stop downloading a file.
     */
    void stopDownloading();

    interface Callback {

        void onBytesRead(byte[] buffer, int bytesRead);

        void onError(String cause);

        void onDownloadFinished();
    }
}
