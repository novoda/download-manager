package com.novoda.downloadmanager;

import android.content.Context;

class DownloadsNetworkRecoveryCreator {

    private static DownloadsNetworkRecovery INSTANCE;

    static void createDisabled() {
        DownloadsNetworkRecoveryCreator.INSTANCE = DownloadsNetworkRecovery.DISABLED;
    }

    static void createEnabled(Context context, DownloadManager downloadManager, ConnectionType connectionType) {
        DownloadsNetworkRecoveryCreator.INSTANCE = new LiteDownloadsNetworkRecoveryEnabled(context, downloadManager, connectionType);
    }

    static DownloadsNetworkRecovery getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("There is no instance available, make sure you call DownloadsNetworkRecoveryCreator.create(...) first");
        } else {
            return INSTANCE;
        }
    }
}
