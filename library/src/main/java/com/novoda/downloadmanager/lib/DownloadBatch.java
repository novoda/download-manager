package com.novoda.downloadmanager.lib;

import java.util.List;

class DownloadBatch {

    public static final DownloadBatch DELETED = new DownloadBatch(-1, null, null, -1, -1L, -1L);

    private final long batchId;
    private final BatchInfo info;
    private final List<FileDownloadInfo> downloads;
    private final int status;
    private final long totalSizeBytes;
    private final long currentSizeBytes;

    public DownloadBatch(long batchId, BatchInfo info, List<FileDownloadInfo> downloads, int status, long totalSizeBytes, long currentSizeBytes) {
        this.batchId = batchId;
        this.info = info;
        this.downloads = downloads;
        this.status = status;
        this.totalSizeBytes = totalSizeBytes;
        this.currentSizeBytes = currentSizeBytes;
    }

    public long getBatchId() {
        return batchId;
    }

    public long getTotalSize() {
        return totalSizeBytes;
    }

    public long getCurrentSize() {
        return currentSizeBytes;
    }

    public BatchInfo getInfo() {
        return info;
    }

    public List<FileDownloadInfo> getDownloads() {
        return downloads;
    }

    public int getStatus() {
        return status;
    }

    public boolean isDeleted() {
        return this == DELETED;
    }

    /**
     * Return time when this download will be ready for its next action, in
     * milliseconds after given time.
     *
     * @return If {@code 0}, download is ready to proceed immediately. If
     * {@link Long#MAX_VALUE}, then download has no future actions.
     */
    public long nextActionMillis(long now, long nextRetryTimeMillis) {
        for (FileDownloadInfo info : downloads) {
            long individualRetryTimeMillis = getNextActionMillisFor(now, info);
            nextRetryTimeMillis = Math.min(individualRetryTimeMillis, nextRetryTimeMillis);
        }
        return nextRetryTimeMillis;
    }

    private long getNextActionMillisFor(long now, FileDownloadInfo info) {
        if (DownloadStatus.isCompleted(status)) {
            return Long.MAX_VALUE;
        }
        if (status != DownloadStatus.WAITING_TO_RETRY) {
            return 0;
        }
        long when = info.restartTime(now);
        if (when <= now) {
            return 0;
        }
        return when - now;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DownloadBatch that = (DownloadBatch) o;
        return batchId == that.batchId;

    }

    @Override
    public int hashCode() {
        return (int) (batchId ^ (batchId >>> 32));
    }

    public boolean prune(DownloadDeleter downloadDeleter) {
        boolean isDeleted = false;

        for (FileDownloadInfo info : downloads) {
            if (info.isDeleted()) {
                downloadDeleter.deleteFileAndDatabaseRow(info);
                isDeleted = true;
            } else if (DownloadStatus.isCancelled(info.getStatus()) || DownloadStatus.isError(info.getStatus())) {
                downloadDeleter.deleteFileAndMediaReference(info);
                isDeleted = true;
            }
        }
        return isDeleted;
    }

    public boolean isActive() {
        return status == DownloadStatus.SUBMITTED || status == DownloadStatus.RUNNING;
    }

    public boolean scanCompletedMediaIfReady(DownloadScanner downloadScanner) {
        for (FileDownloadInfo info : downloads) {
            if (info.startScanIfReady(downloadScanner)) {
                return true;
            }
        }
        return false;
    }

}
