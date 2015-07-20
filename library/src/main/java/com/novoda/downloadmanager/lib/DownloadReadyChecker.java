package com.novoda.downloadmanager.lib;

import android.os.Environment;

class DownloadReadyChecker {

    private final SystemFacade systemFacade;
    private final NetworkChecker networkChecker;
    private final DownloadClientReadyChecker downloadClientReadyChecker;
    private final PublicFacingDownloadMarshaller downloadMarshaller;

    DownloadReadyChecker(SystemFacade systemFacade, NetworkChecker networkChecker, DownloadClientReadyChecker downloadClientReadyChecker,
                         PublicFacingDownloadMarshaller downloadMarshaller) {
        this.systemFacade = systemFacade;
        this.networkChecker = networkChecker;
        this.downloadClientReadyChecker = downloadClientReadyChecker;
        this.downloadMarshaller = downloadMarshaller;
    }

    public boolean canDownload(DownloadBatch downloadBatch) {
        if (downloadBatch.getStatus() == DownloadStatus.PENDING) {
            return true;
        }

        for (FileDownloadInfo fileDownloadInfo : downloadBatch.getDownloads()) {
            if (!isDownloadManagerReadyToDownload(fileDownloadInfo)) {
                return false;
            }
        }

        return downloadClientReadyChecker.isAllowedToDownload(downloadMarshaller.marshall(downloadBatch));
    }

    /**
     * Returns whether this download should be enqueued.
     */
    private boolean isDownloadManagerReadyToDownload(FileDownloadInfo downloadInfo) {
        if (downloadInfo.getControl() == DownloadsControl.CONTROL_PAUSED) {
            // the download is paused, so it's not going to start
            return false;
        }
        switch (downloadInfo.getStatus()) {
            case 0: // status hasn't been initialized yet, this is a new download
            case DownloadStatus.PENDING: // download is explicit marked as ready to start
            case DownloadStatus.RUNNING: // download interrupted (process killed etc) while
                // running, without a chance to update the database
                return true;

            case DownloadStatus.WAITING_FOR_NETWORK:
            case DownloadStatus.QUEUED_FOR_WIFI:
                return networkChecker.checkCanUseNetwork(downloadInfo) == FileDownloadInfo.NetworkState.OK;

            case DownloadStatus.WAITING_TO_RETRY:
                // download was waiting for a delayed restart
                final long now = systemFacade.currentTimeMillis();
                return downloadInfo.restartTime(now) <= now;
            case DownloadStatus.DEVICE_NOT_FOUND_ERROR:
                // is the media mounted?
                return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
            case DownloadStatus.INSUFFICIENT_SPACE_ERROR:
                // avoids repetition of retrying download
                return false;
        }
        return false;
    }

}
