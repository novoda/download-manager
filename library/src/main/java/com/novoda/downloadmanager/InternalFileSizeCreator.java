package com.novoda.downloadmanager;

final class InternalFileSizeCreator {

    private static final int ZERO_BYTES = 0;
    private static final InternalFileSize UNKNOWN = new LiteFileSize(ZERO_BYTES, ZERO_BYTES);

    private InternalFileSizeCreator() {
        // Uses static factory methods.
    }

    static InternalFileSize unknownFileSize() {
        return UNKNOWN.copy();
    }

    static InternalFileSize createFromCurrentAndTotalSize(long currentSize, long totalSize) {
        return new LiteFileSize(currentSize, totalSize);
    }
}
