package com.novoda.downloadmanager;

public interface FileSize {

    long currentSize();

    long totalSize();

    boolean isTotalSizeKnown();

    boolean isTotalSizeUnknown();

    boolean areBytesDownloadedKnown();
}
