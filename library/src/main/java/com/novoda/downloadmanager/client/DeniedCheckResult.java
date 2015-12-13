package com.novoda.downloadmanager.client;

import com.novoda.downloadmanager.domain.DownloadId;

public class DeniedCheckResult implements ClientCheckResult {

    private final DownloadId downloadId;
    private final String reason;

    public DeniedCheckResult(DownloadId downloadId, String reason) {
        this.downloadId = downloadId;
        this.reason = reason;
    }

    @Override
    public boolean isAllowed() {
        return false;
    }

    @Override
    public String reason() {
        return reason;
    }

    @Override
    public DownloadId id() {
        return downloadId;
    }

}
