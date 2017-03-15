package com.novoda.downloadmanager.lib.jobscheduler;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.novoda.downloadmanager.lib.DownloadServiceJob;
import com.novoda.downloadmanager.lib.logger.LLog;

import java.util.concurrent.TimeUnit;

public class DownloadJob extends Job {

    private static final long BACKOFF_MILLIS = TimeUnit.SECONDS.toMillis(2);
    private static final long EXECUTION_START_MILLIS = 500L;
    private static final long EXECUTION_END_MILLIS = 500L;

    static String TAG = "download_job_tag";

    private static DownloadServiceJob downloadServiceJob;

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        LLog.v("Ferran, job starts right now");
        downloadServiceJob.onStartCommand();
        return Result.SUCCESS;
    }

    public static void scheduleJob() {
        LLog.v("Ferran, scheduling a job to start immediatelly in 500ms");
        scheduleJob(EXECUTION_START_MILLIS);
    }

    public static void scheduleJob(final long startMillis) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                downloadServiceJob = DownloadServiceJob.getInstance();

                new JobRequest.Builder(TAG)
                        .setExecutionWindow(startMillis, startMillis + EXECUTION_END_MILLIS)
                        .setBackoffCriteria(BACKOFF_MILLIS, JobRequest.BackoffPolicy.EXPONENTIAL)
                        .setRequiresDeviceIdle(false)
                        .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                        .setRequirementsEnforced(true)
                        .setPersisted(true)
                        .build()
                        .schedule();
            }
        });
    }
}
