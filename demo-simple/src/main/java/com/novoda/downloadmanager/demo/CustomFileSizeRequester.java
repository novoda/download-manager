package com.novoda.downloadmanager.demo;

import com.novoda.downloadmanager.FileSize;
import com.novoda.downloadmanager.FileSizeCreator;
import com.novoda.downloadmanager.FileSizeRequester;

class CustomFileSizeRequester implements FileSizeRequester {

    private static final long FILE_TOTAL_SIZE = 1000;

    @Override
    public FileSize requestFileSize(String url) {
        return FileSizeCreator.createFromTotalSize(FILE_TOTAL_SIZE);
    }

    @Override
    public void requestFileSize(String url, Callback callback) {
        callback.onFileSizeReceived(FileSizeCreator.createFromTotalSize(FILE_TOTAL_SIZE));
    }
}
