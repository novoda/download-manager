package com.novoda.downloadmanager;

import android.content.Context;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.novoda.notils.logger.simple.Log;

import java.util.concurrent.TimeUnit;

class LiteDownloadsNetworkRecoveryEnabled implements DownloadsNetworkRecovery {

    private static final long ONE_SECOND_IN_MILLIS = TimeUnit.SECONDS.toMillis(1);
    private static final long FIVE_MINUTES_IN_MILLIS = TimeUnit.MINUTES.toMillis(5);

    private ConnectionType connectionType;

    LiteDownloadsNetworkRecoveryEnabled(Context context, DownloadManager downloadManager, ConnectionType connectionType) {
        this.connectionType = connectionType;
        JobManager jobManager = JobManager.create(context);
        jobManager.addJobCreator(new LiteJobCreator(downloadManager));
    }

    @Override
    public void scheduleRecovery() {
        JobRequest.Builder builder = new JobRequest.Builder(LiteJobCreator.TAG)
                .setExecutionWindow(ONE_SECOND_IN_MILLIS, FIVE_MINUTES_IN_MILLIS);

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

        jobManager.cancelAllForTag(LiteJobCreator.TAG);
        jobManager.schedule(jobRequest);
        Log.v("Scheduling Network Recovery.");
    }

    @Override
    public void updateAllowedConnectionType(ConnectionType allowedConnectionType) {
        connectionType = allowedConnectionType;
    }
}
