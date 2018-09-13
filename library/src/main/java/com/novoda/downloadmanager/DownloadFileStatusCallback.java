package com.novoda.downloadmanager;

import android.support.annotation.Nullable;

/**
 * Given to the asynchronous call
 * {@link DownloadManager#getDownloadFileStatusWithMatching(DownloadBatchId, DownloadFileId, DownloadFileStatusCallback)} to receive the
 * {@link DownloadFileStatus} of a {@link DownloadFile} matching the given {@link DownloadBatchId} and {@link DownloadFileId}.
 */
public interface DownloadFileStatusCallback {

    void onReceived(@Nullable DownloadFileStatus downloadFileStatus);
}
