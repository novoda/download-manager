package com.novoda.downloadmanager.demo;

import com.novoda.downloadmanager.FileSizeCreator;
import com.novoda.downloadmanager.FileSizeRequester;
import com.novoda.downloadmanager.FileSizeResult;

class CustomFileSizeRequester implements FileSizeRequester {

    private static final long FILE_TOTAL_SIZE = 1000;

    @Override
    public FileSizeResult requestFileSizeResult(String url) {
        return FileSizeResult.success(FileSizeCreator.createFromTotalSize(FILE_TOTAL_SIZE));
    }
}
