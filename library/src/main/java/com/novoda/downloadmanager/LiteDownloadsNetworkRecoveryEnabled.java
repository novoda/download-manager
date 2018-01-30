package com.novoda.downloadmanager;

import android.content.Context;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.novoda.notils.logger.simple.Log;

import java.util.concurrent.TimeUnit;

class LiteDownloadsNetworkRecoveryEnabled implements DownloadsNetworkRecovery {

    private ConnectionType connectionType;

    LiteDownloadsNetworkRecoveryEnabled(Context context, DownloadManager downloadManager, ConnectionType connectionType) {
        this.connectionType = connectionType;
        JobManager jobManager = JobManager.create(context);
        jobManager.addJobCreator(new LiteJobCreator(downloadManager));
    }

    @Override
    public void scheduleRecovery() {
        JobRequest.Builder builder = new JobRequest.Builder(LiteJobCreator.TAG)
                .setExecutionWindow(TimeUnit.SECONDS.toMillis(1), TimeUnit.DAYS.toMillis(1));

        switch (connectionType) {
            case ALL:
                builder.setRequiredNetworkType(JobRequest.NetworkType.CONNECTED);
                break;
            case UNMETERED:
                builder.setRequiredNetworkType(JobRequest.NetworkType.UNMETERED);
                break;
            case METERED:
                builder.setRequiredNetworkType(JobRequest.NetworkType.METERED);
                break;
            default:
                Log.w("Unknown ConnectionType: " + connectionType);
                break;
        }

        JobRequest jobRequest = builder.build();
        JobManager jobManager = JobManager.instance();

        jobManager.schedule(jobRequest);
        Log.v("Scheduling Network Recovery.");
    }

    @Override
    public void updateAllowedConnection(ConnectionType allowedConnectionType) {
        connectionType = allowedConnectionType;
    }
}
