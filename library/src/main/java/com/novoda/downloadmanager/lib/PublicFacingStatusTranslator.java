package com.novoda.downloadmanager.lib;

public class PublicFacingStatusTranslator {

    public int translate(int status) {
        switch (status) {
            case DownloadStatus.SUBMITTED:
            case DownloadStatus.PENDING:
                return DownloadManager.STATUS_PENDING;
            case DownloadStatus.RUNNING:
                return DownloadManager.STATUS_RUNNING;
            case DownloadStatus.QUEUED_DUE_CLIENT_RESTRICTIONS:
            case DownloadStatus.PAUSED_BY_APP:
            case DownloadStatus.WAITING_TO_RETRY:
            case DownloadStatus.WAITING_FOR_NETWORK:
            case DownloadStatus.QUEUED_FOR_WIFI:
                return DownloadManager.STATUS_PAUSED;
            case DownloadStatus.PAUSING:
                return DownloadManager.STATUS_PAUSING;
            case DownloadStatus.SUCCESS:
                return DownloadManager.STATUS_SUCCESSFUL;
            case DownloadStatus.DELETING:
                return DownloadManager.STATUS_DELETING;
            default:
                return DownloadManager.STATUS_FAILED;
        }
    }
}
