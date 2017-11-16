package com.novoda.downloadmanager;

interface InternalFileSize extends FileSize {

    void addToCurrentSize(long newBytes);

    void setTotalSize(long totalSize);

    void setCurrentSize(long currentSize);

    InternalFileSize copy();
}
