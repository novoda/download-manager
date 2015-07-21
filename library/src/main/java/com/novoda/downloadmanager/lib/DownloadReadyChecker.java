package com.novoda.downloadmanager.lib;

import android.os.Environment;

import java.util.List;

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
        if (isDownloadManagerReadyToDownload(downloadBatch)) {
            return downloadClientReadyChecker.isAllowedToDownload(downloadMarshaller.marshall(downloadBatch));
        }

        return false;
    }

    private boolean isDownloadManagerReadyToDownload(DownloadBatch downloadBatch) {
        List<FileDownloadInfo> downloads = downloadBatch.getDownloads();

        if (isThereAPausedDownload(downloads)) {
            return false;
        }

        switch (downloadBatch.getStatus()) {
            case DownloadStatus.PENDING: // download is explicit marked as ready to start
            case DownloadStatus.RUNNING: // download interrupted (process killed etc) while
                // running, without a chance to update the database
                return true;

            case DownloadStatus.WAITING_FOR_NETWORK:
            case DownloadStatus.QUEUED_FOR_WIFI:
                return isThereADownloadThatCanUseNetwork(downloads);

            case DownloadStatus.WAITING_TO_RETRY:
                // download was waiting for a delayed restart
                long now = systemFacade.currentTimeMillis();
                return isThereARetryDownloadThatCanRestart(downloads, now);
            case DownloadStatus.DEVICE_NOT_FOUND_ERROR:
                // is the media mounted?
                return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
            case DownloadStatus.INSUFFICIENT_SPACE_ERROR:
                // avoids repetition of retrying download
                return false;
            default:
                return false;
        }
    }

    private boolean isThereAPausedDownload(List<FileDownloadInfo> downloadBatch) {
        for (FileDownloadInfo download : downloadBatch) {
            if (download.getControl() == DownloadsControl.CONTROL_PAUSED) {
                return true;
            }
        }

        return false;
    }

    private boolean isThereADownloadThatCanUseNetwork(List<FileDownloadInfo> downloadBatch) {
        for (FileDownloadInfo download : downloadBatch) {
            if (networkChecker.checkCanUseNetwork(download) == FileDownloadInfo.NetworkState.OK) {
                return true;
            }
        }

        return false;
    }

    private boolean isThereARetryDownloadThatCanRestart(List<FileDownloadInfo> downloadBatch, long now) {
        for (FileDownloadInfo download : downloadBatch) {
            if (download.restartTime(now) <= now) {
                return true;
            }
        }

        return false;
    }

}
