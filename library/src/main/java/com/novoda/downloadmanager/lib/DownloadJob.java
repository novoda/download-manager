package com.novoda.downloadmanager.lib;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.novoda.downloadmanager.lib.logger.LLog;

import java.util.concurrent.TimeUnit;

class DownloadJob extends Job {

    public static final String TAG = "download_job_tag";

    private static final long BACKOFF_MILLIS = TimeUnit.SECONDS.toMillis(5);
    private static final long EXECUTION_START_MILLIS = TimeUnit.SECONDS.toMillis(1);
    private static final long EXECUTION_END_MILLIS = TimeUnit.SECONDS.toMillis(10);

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        Context context = GlobalState.getContext();
        context.startService(new Intent(context, DownloadService.class));
        return Result.SUCCESS;
    }

    public static void scheduleJob() {
        LLog.v("scheduling a job to start in " + EXECUTION_START_MILLIS + "ms");
        scheduleJob(EXECUTION_START_MILLIS);
    }

    private static void scheduleJob(final long startMillis) {
        new JobRequest.Builder(TAG)
                .setExecutionWindow(startMillis, EXECUTION_END_MILLIS)
                .setBackoffCriteria(BACKOFF_MILLIS, JobRequest.BackoffPolicy.LINEAR)
                .setRequiresDeviceIdle(false)
                .setRequirementsEnforced(true)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setPersisted(true)
                .build()
                .schedule();
    }
}
