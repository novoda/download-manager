package com.novoda.downloadmanager;

import com.novoda.merlin.MerlinsBeard;

class DownloadConnectionAllowedChecker {

    private final MerlinsBeard merlinsBeard;
    private ConnectionType allowedConnectionType;

    DownloadConnectionAllowedChecker(MerlinsBeard merlinsBeard, ConnectionType allowedConnectionType) {
        this.merlinsBeard = merlinsBeard;
        this.allowedConnectionType = allowedConnectionType;
    }

    boolean isAllowedToDownload() {
        switch (allowedConnectionType) {
            case UNMETERED:
                return merlinsBeard.isConnectedToWifi();
            case METERED:
                return merlinsBeard.isConnectedToMobileNetwork();
            default:
                return true;
        }
    }

    void updateAllowedConnectionType(ConnectionType allowedConnectionType) {
        this.allowedConnectionType = allowedConnectionType;
    }
}
