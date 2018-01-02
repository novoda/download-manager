package com.novoda.downloadmanager;

class LiteFileSize implements InternalFileSize {

    private static final int ZERO_BYTES = 0;

    private long currentSize;
    private long totalSize;

    LiteFileSize(long currentSize, long totalSize) {
        this.currentSize = currentSize;
        this.totalSize = totalSize;
    }

    @Override
    public boolean isTotalSizeUnknown() {
        return totalSize <= ZERO_BYTES;
    }

    @Override
    public boolean isTotalSizeKnown() {
        return totalSize > ZERO_BYTES;
    }

    @Override
    public boolean areBytesDownloadedKnown() {
        return currentSize > ZERO_BYTES;
    }

    @Override
    public long currentSize() {
        return currentSize;
    }

    @Override
    public long totalSize() {
        return totalSize;
    }

    @Override
    public void addToCurrentSize(long newBytes) {
        currentSize += newBytes;
    }

    @Override
    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    @Override
    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    @Override
    public LiteFileSize copy() {
        return new LiteFileSize(currentSize, totalSize);
    }

    @Override
    public String toString() {
        return "LiteFileSize{"
                + "currentSize=" + currentSize
                + ", totalSize=" + totalSize
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LiteFileSize that = (LiteFileSize) o;

        if (currentSize != that.currentSize) {
            return false;
        }
        return totalSize == that.totalSize;
    }

    @Override
    public int hashCode() {
        int result = (int) (currentSize ^ (currentSize >>> 32));
        result = 31 * result + (int) (totalSize ^ (totalSize >>> 32));
        return result;
    }
}
