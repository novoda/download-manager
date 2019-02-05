package com.novoda.downloadmanager;

class InMemoryFileSizeRequester implements FileSizeRequester {

    private static final long TOTAL_FILE_SIZE = 5000000;

    @Override
    public FileSize requestFileSize(String url) {
        return FileSizeCreator.createFromTotalSize(TOTAL_FILE_SIZE);
    }

    @Override
    public FileSizeResult requestFileSizeResult(String url) {
        return null;
    }
}
