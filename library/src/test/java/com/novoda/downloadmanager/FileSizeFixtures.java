package com.novoda.downloadmanager;

class FileSizeFixtures {

    private long currentSize = 0;
    private long totalSize = 1000;
    private boolean isTotalSizeKnown = true;
    private boolean isTotalSizeUnknown = false;
    private boolean areBytesDownloadedKnown = true;

    static FileSizeFixtures aFileSize() {
        return new FileSizeFixtures();
    }

    FileSizeFixtures withCurrentSize(long currentSize) {
        this.currentSize = currentSize;
        return this;
    }

    FileSizeFixtures withTotalSize(long totalSize) {
        this.totalSize = totalSize;
        return this;
    }

    FileSizeFixtures withTotalSizeKnown(boolean totalSizeKnown) {
        isTotalSizeKnown = totalSizeKnown;
        return this;
    }

    FileSizeFixtures withTotalSizeUnknown(boolean totalSizeUnknown) {
        isTotalSizeUnknown = totalSizeUnknown;
        return this;
    }

    FileSizeFixtures withAreBytesDownloadedKnown(boolean areBytesDownloadedKnown) {
        this.areBytesDownloadedKnown = areBytesDownloadedKnown;
        return this;
    }

    FileSize build() {
        return new FileSize() {
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
