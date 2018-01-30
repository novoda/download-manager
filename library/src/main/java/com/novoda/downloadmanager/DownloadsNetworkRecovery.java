package com.novoda.downloadmanager;

interface DownloadsNetworkRecovery {

    void scheduleRecovery();

    void updateAllowedConnection(ConnectionType allowedConnectionType);

    DownloadsNetworkRecovery DISABLED = new DownloadsNetworkRecovery() {
        @Override
        public void scheduleRecovery() {
            // do-nothing
        }

        @Override
        public void updateAllowedConnection(ConnectionType allowedConnectionType) {
            // do-nothing
        }
    };
}
