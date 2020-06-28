package com.novoda.downloadmanager;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.WorkManager;

class LiteDownloadsNetworkRecoveryEnabled implements DownloadsNetworkRecovery {

    private final WorkManager workManager;
    private ConnectionType connectionType;

    LiteDownloadsNetworkRecoveryEnabled(WorkManager workManager, ConnectionType connectionType) {
        this.connectionType = connectionType;
        this.workManager = workManager;
    }

    @Override
    public void scheduleRecovery() {
        Constraints.Builder builder = new Constraints.Builder();

        switch (connectionType) {
            case ALL:
                builder.setRequiredNetworkType(NetworkType.CONNECTED);
                break;
            case UNMETERED:
                builder.setRequiredNetworkType(NetworkType.UNMETERED);
                break;
            case METERED:
                builder.setRequiredNetworkType(NetworkType.METERED);
                break;
            default:
                Logger.w("Unknown ConnectionType: " + connectionType);
                break;
        }

        workManager.cancelAllWorkByTag(LiteJobCreator.TAG);
        workManager.enqueue(LiteJobDownload.newInstance(LiteJobCreator.TAG, builder.build()));
        Logger.v("Scheduling Network Recovery.");
    }

    @Override
    public void updateAllowedConnectionType(ConnectionType allowedConnectionType) {
        connectionType = allowedConnectionType;
    }
}
