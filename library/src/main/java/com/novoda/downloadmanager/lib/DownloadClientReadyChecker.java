package com.novoda.downloadmanager.lib;

public interface DownloadClientReadyChecker {
    Ready READY = new Ready();

    /**
     * This method is executed on a background thread
     * @param collatedDownloadInfo
     */
    boolean isAllowedToDownload(CollatedDownloadInfo collatedDownloadInfo);

    /**
     * Ready specifies that it is always ready to download
     */
    class Ready implements DownloadClientReadyChecker {

        @Override
        public boolean isAllowedToDownload(CollatedDownloadInfo collatedDownloadInfo) {
            return true;
        }
    }
}
