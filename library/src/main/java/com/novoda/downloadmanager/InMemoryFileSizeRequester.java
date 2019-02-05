package com.novoda.downloadmanager;

class InMemoryFileSizeRequester implements FileSizeRequester {

    private static final long TOTAL_FILE_SIZE = 5000000;

    @Override
    public FileSizeResult requestFileSize(String url) {
        return FileSizeResult.success(FileSizeCreator.createFromTotalSize(TOTAL_FILE_SIZE));
    }
}
