package com.novoda.downloadmanager.lib;

import com.novoda.downloadmanager.Download;

public interface DownloadClientReadyChecker {
    Ready READY = new Ready();

    /**
     * This method is executed on a background thread
     */
    boolean isAllowedToDownload(Download download);

    /**
     * Ready specifies that it is always ready to download
     */
    class Ready implements DownloadClientReadyChecker {

        @Override
        public boolean isAllowedToDownload(Download download) {
            return true;
        }
    }
}
