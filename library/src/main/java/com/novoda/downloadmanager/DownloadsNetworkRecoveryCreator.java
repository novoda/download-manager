package com.novoda.downloadmanager;

import androidx.work.WorkManager;

final class DownloadsNetworkRecoveryCreator {

    private static DownloadsNetworkRecovery singleInstance;

    private DownloadsNetworkRecoveryCreator() {
        // Uses static factory methods.
    }

    static void createDisabled() {
        DownloadsNetworkRecoveryCreator.singleInstance = DownloadsNetworkRecovery.DISABLED;
    }

    static void createEnabled(WorkManager workManager, ConnectionType connectionType) {
        DownloadsNetworkRecoveryCreator.singleInstance = new LiteDownloadsNetworkRecoveryEnabled(workManager, connectionType);
    }

    static DownloadsNetworkRecovery getInstance() {
        if (singleInstance == null) {
            throw new IllegalStateException("There is no instance available, make sure you call DownloadsNetworkRecoveryCreator.create(...) first");
        } else {
            return singleInstance;
        }
    }
}
