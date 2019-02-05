package com.novoda.downloadmanager;

/**
 * For defining the mechanism by which a file size is determined.
 * Clients can create their own implementation and pass to {@link DownloadManagerBuilder#withFileDownloaderCustom(FileSizeRequester, Class)}.
 */
public interface FileSizeRequester {

    FileSizeResult requestFileSizeResult(String url);

    interface Callback {
        void onFileSizeReceived(FileSize fileSize);

        void onError(String message);
    }
}
