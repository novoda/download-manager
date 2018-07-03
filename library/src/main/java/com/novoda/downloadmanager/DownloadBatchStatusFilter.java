package com.novoda.downloadmanager;

class DownloadBatchStatusFilter {

    private InternalDownloadBatchStatus currentStatus;

    boolean shouldFilterOut(DownloadBatchStatus currentDownloadBatchStatus) {
        if (!(currentDownloadBatchStatus instanceof InternalDownloadBatchStatus)) {
            return true;
        }

        InternalDownloadBatchStatus copiedStatus = ((InternalDownloadBatchStatus) currentDownloadBatchStatus).copy();
        if (copiedStatus.equals(currentStatus)) {
            return true;
        }

        currentStatus = copiedStatus;
        return false;
    }
}
