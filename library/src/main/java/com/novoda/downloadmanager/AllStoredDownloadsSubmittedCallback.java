package com.novoda.downloadmanager;

/**
 * Given to the asynchronous call {@link LiteDownloadManagerCommands#submitAllStoredDownloads(AllStoredDownloadsSubmittedCallback)}
 * that notifies when all currently stored download batches have been submitted for download.
 */
public interface AllStoredDownloadsSubmittedCallback {

    void onAllDownloadsSubmitted();
}
