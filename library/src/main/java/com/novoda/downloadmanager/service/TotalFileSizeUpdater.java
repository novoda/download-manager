package com.novoda.downloadmanager.service;

import com.novoda.downloadmanager.domain.DownloadFile;
import com.novoda.downloadmanager.download.ContentLengthFetcher;
import com.novoda.downloadmanager.download.DownloadDatabaseWrapper;

import java.util.List;

class TotalFileSizeUpdater {

    private final DownloadDatabaseWrapper downloadDatabaseWrapper;
    private final ContentLengthFetcher contentLengthFetcher;

    TotalFileSizeUpdater(DownloadDatabaseWrapper downloadDatabaseWrapper, ContentLengthFetcher contentLengthFetcher) {
        this.downloadDatabaseWrapper = downloadDatabaseWrapper;
        this.contentLengthFetcher = contentLengthFetcher;
    }

    public void updateMissingTotalFileSizes() {
        List<DownloadFile> files = downloadDatabaseWrapper.getFilesWithUnknownTotalSize();
        for (DownloadFile file : files) {
            updateTotalSizeFor(file);
        }
    }

    private void updateTotalSizeFor(DownloadFile file) {
        long totalBytes = contentLengthFetcher.fetchContentLengthFor(file);
        downloadDatabaseWrapper.updateFileSize(file, totalBytes);
    }
}
