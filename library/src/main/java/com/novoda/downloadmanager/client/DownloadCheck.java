package com.novoda.downloadmanager.client;

import com.novoda.downloadmanager.domain.Download;

import java.io.Serializable;

public interface DownloadCheck extends Serializable {

    DownloadCheck IGNORED = new DownloadCheck() {
        @Override
        public ClientCheckResult isAllowedToDownload(Download download) {
            return ClientCheckResult.ALLOWED;
        }
    };

    ClientCheckResult isAllowedToDownload(Download download);

}
