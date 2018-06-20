package com.novoda.downloadmanager;

/**
 * For defining the mechanism by which a file size is determined.
 */
public interface FileSizeRequester {

    FileSize requestFileSize(String url);
}
