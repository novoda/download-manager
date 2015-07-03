package com.novoda.downloadmanager.lib;

public interface DownloadClientReadyChecker {
    Ready READY = new Ready();

    /**
     * This method is executed on a background thread
     *
     * @param downloadBatch
     */
    boolean isAllowedToDownload(DownloadBatch downloadBatch);

    /**
     * Ready specifies that it is always ready to download
     */
    class Ready implements DownloadClientReadyChecker {

        @Override
        public boolean isAllowedToDownload(DownloadBatch downloadBatch) {
            return true;
        }
    }
}
