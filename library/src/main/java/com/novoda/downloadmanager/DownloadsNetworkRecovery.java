package com.novoda.downloadmanager;

interface DownloadsNetworkRecovery {

    void scheduleRecovery();

    DownloadsNetworkRecovery DISABLED = new DownloadsNetworkRecovery() {
        @Override
        public void scheduleRecovery() {
            // do-nothing
        }
    };
}
