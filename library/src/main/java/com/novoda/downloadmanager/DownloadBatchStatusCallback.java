package com.novoda.downloadmanager;

/**
 * Given to the asynchronous call {@link DownloadManager#addDownloadBatchCallback(DownloadBatchStatusCallback)},
 * to receive {@link DownloadBatchStatus} updates as they are emitted by the download-manager.
 */
public interface DownloadBatchStatusCallback {

    void onUpdate(DownloadBatchStatus downloadBatchStatus);
}
