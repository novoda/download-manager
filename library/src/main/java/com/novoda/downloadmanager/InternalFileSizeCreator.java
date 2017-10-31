package com.novoda.downloadmanager;

class InternalFileSizeCreator {

    private static final int ZERO_BYTES = 0;

    static InternalFileSize UNKNOWN = new LiteFileSize(ZERO_BYTES, ZERO_BYTES);

    static InternalFileSize createFromCurrentAndTotalSize(long currentSize, long totalSize) {
        return new LiteFileSize(currentSize, totalSize);
    }
}
