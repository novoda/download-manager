package com.novoda.downloadmanager;

public interface FileSize {

    long currentSize();

    long totalSize();

    long remainingSize();

    boolean isTotalSizeKnown();

    boolean isTotalSizeUnknown();

    boolean areBytesDownloadedKnown();
}
