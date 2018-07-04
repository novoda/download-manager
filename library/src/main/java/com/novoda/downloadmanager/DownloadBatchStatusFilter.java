package com.novoda.downloadmanager;

class DownloadBatchStatusFilter {

    private InternalDownloadBatchStatus currentStatus;

    boolean shouldFilterOut(DownloadBatchStatus currentDownloadBatchStatus) {
        if (!(currentDownloadBatchStatus instanceof InternalDownloadBatchStatus)) {
            Logger.w(currentDownloadBatchStatus.getClass() + " is not an instance of " + InternalDownloadBatchStatus.class);
            return true;
        }

        InternalDownloadBatchStatus copiedStatus = ((InternalDownloadBatchStatus) currentDownloadBatchStatus).copy();
        if (copiedStatus.equals(currentStatus)) {
            Logger.v("Failed filter. "
                             + "ID: " + currentDownloadBatchStatus.getDownloadBatchId().rawId()
                             + " Status: " + currentDownloadBatchStatus.status().toRawValue()
            );
            return true;
        }

        Logger.v(
                "Passes filter. "
                        + "ID: " + currentDownloadBatchStatus.getDownloadBatchId().rawId()
                        + " Status: " + currentDownloadBatchStatus.status().toRawValue()
        );
        currentStatus = copiedStatus;
        return false;
    }
}
