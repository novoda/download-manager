package com.novoda.downloadmanager;

class DeniedCheckResult implements ClientCheckResult {

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
