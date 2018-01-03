package com.novoda.downloadmanager;

import android.content.Context;

final class DownloadsNetworkRecoveryCreator {

    private static DownloadsNetworkRecovery singleInstance;

    private DownloadsNetworkRecoveryCreator() {
        // Uses static factory methods.
    }

    static void createDisabled() {
        DownloadsNetworkRecoveryCreator.singleInstance = DownloadsNetworkRecovery.DISABLED;
    }

    static void createEnabled(Context context, DownloadManager downloadManager, ConnectionType connectionType) {
        DownloadsNetworkRecoveryCreator.singleInstance = new LiteDownloadsNetworkRecoveryEnabled(context, downloadManager, connectionType);
    }

    static DownloadsNetworkRecovery getInstance() {
        if (singleInstance == null) {
            throw new IllegalStateException("There is no instance available, make sure you call DownloadsNetworkRecoveryCreator.create(...) first");
        } else {
            return singleInstance;
        }
    }
}
