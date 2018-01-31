package com.novoda.downloadmanager;

interface DownloadsNetworkRecovery {

    void scheduleRecovery();

    void updateAllowedConnectionType(ConnectionType allowedConnectionType);

    DownloadsNetworkRecovery DISABLED = new DownloadsNetworkRecovery() {
        @Override
        public void scheduleRecovery() {
            // do-nothing
        }

        @Override
        public void updateAllowedConnectionType(ConnectionType allowedConnectionType) {
            // do-nothing
        }
    };
}
