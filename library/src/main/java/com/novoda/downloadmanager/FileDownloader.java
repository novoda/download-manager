package com.novoda.downloadmanager;

/**
 * For defining the mechanism by which files are downloaded.
 */
public interface FileDownloader {

    /**
     * Called internally to start downloading a file.
     *
     * @param requestUrl of the asset to download.
     * @param fileSize   the byte ranges, represented as file sizes, used to download an asset.
     * @param callback   that is notified of download progress.
     */
    void startDownloading(String requestUrl, FileSize fileSize, Callback callback);

    /**
     * Called internally to stop downloading a file.
     */
    void stopDownloading();

    interface Callback {

        void onBytesRead(byte[] buffer, int bytesRead);

        void onError(Error error);

        void onDownloadFinished();
    }

    /**
     * Represents error information that is accessible to clients.
     */
    interface Error {

        int UNEXPECTED_ERROR_CODE = -1;

        /**
         * Use to create instances of {@link LiteFileDownloadError}.
         *
         * @param rawRequest the raw request from which the error occurs.
         * @param message    any message associated with the error.
         * @param errorCode  the code associated with the error.
         * @return an instance of {@link Error}.
         */
        static Error createFrom(String rawRequest, String message, int errorCode) {
            return new LiteFileDownloadError(rawRequest, message, errorCode);
        }

        /**
         * @return the raw request performed when the error occurred.
         */
        String requestUrl();

        /**
         * @return a message representing the error that occurred.
         */
        String message();

        /**
         * @return a short code representing the error that occurred.
         */
        int errorCode();
    }
}
