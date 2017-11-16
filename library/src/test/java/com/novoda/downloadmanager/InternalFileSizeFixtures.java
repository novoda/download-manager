package com.novoda.downloadmanager;

class InternalFileSizeFixtures {

    private long currentSize = 0;
    private long totalSize = 1000;
    private boolean isTotalSizeKnown = true;
    private boolean isTotalSizeUnknown = false;
    private boolean areBytesDownloadedKnown = true;

    static InternalFileSizeFixtures aFileSize() {
        return new InternalFileSizeFixtures();
    }

    InternalFileSizeFixtures withCurrentSize(long currentSize) {
        this.currentSize = currentSize;
        return this;
    }

    InternalFileSizeFixtures withTotalSize(long totalSize) {
        this.totalSize = totalSize;
        return this;
    }

    InternalFileSizeFixtures withTotalSizeKnown(boolean totalSizeKnown) {
        isTotalSizeKnown = totalSizeKnown;
        return this;
    }

    InternalFileSizeFixtures withTotalSizeUnknown(boolean totalSizeUnknown) {
        isTotalSizeUnknown = totalSizeUnknown;
        return this;
    }

    InternalFileSizeFixtures withAreBytesDownloadedKnown(boolean areBytesDownloadedKnown) {
        this.areBytesDownloadedKnown = areBytesDownloadedKnown;
        return this;
    }

    InternalFileSize build() {
        return new InternalFileSize() {
            @Override
            public void addToCurrentSize(long newBytes) {
                currentSize += newBytes;
            }

            @Override
            public void setTotalSize(long newTotalSize) {
                totalSize = newTotalSize;
            }

            @Override
            public void setCurrentSize(long newCurrentSize) {
                currentSize = newCurrentSize;
            }

            @Override
            public InternalFileSize copy() {
                return InternalFileSizeFixtures.aFileSize()
                        .withCurrentSize(currentSize)
                        .withTotalSize(totalSize)
                        .withTotalSizeKnown(isTotalSizeKnown)
                        .withTotalSizeUnknown(isTotalSizeUnknown)
                        .withAreBytesDownloadedKnown(areBytesDownloadedKnown)
                        .build();
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
            public boolean isTotalSizeKnown() {
                return isTotalSizeKnown;
            }

            @Override
            public boolean isTotalSizeUnknown() {
                return isTotalSizeUnknown;
            }

            @Override
            public boolean areBytesDownloadedKnown() {
                return areBytesDownloadedKnown;
            }
        };
    }
}
