package com.novoda.downloadmanager;

/**
 * Represents the various byte states of a download file.
 */
public interface FileSize {

    long currentSize();

    long totalSize();

    long remainingSize();

    boolean isTotalSizeKnown();

    boolean isTotalSizeUnknown();

    boolean areBytesDownloadedKnown();
}
