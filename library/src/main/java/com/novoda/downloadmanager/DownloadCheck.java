package com.novoda.downloadmanager;

import java.io.Serializable;

interface DownloadCheck extends Serializable {

    DownloadCheck IGNORED = new DownloadCheck() {
        @Override
        public ClientCheckResult isAllowedToDownload(Download download) {
            return ClientCheckResult.ALLOWED;
        }
    };

    ClientCheckResult isAllowedToDownload(Download download);

}
