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

    static String TAG = "download_job_tag";

    private static final long BACKOFF_MILLIS = TimeUnit.SECONDS.toMillis(5);
    private static final long EXECUTION_START_MILLIS = TimeUnit.SECONDS.toMillis(1);
    private static final long EXECUTION_END_MILLIS = TimeUnit.SECONDS.toMillis(2);

    private final Object lock = new Object();

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        final DownloadServiceJob[] downloadServiceJob = new DownloadServiceJob[1];

        LLog.v("Ferran, job starts right now");

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                downloadServiceJob[0] = DownloadServiceJob.getInstance();
                downloadServiceJobInstanceIsReady();
            }
        });

        waitForDownloadServiceJobInstanceToBeReady();

        downloadServiceJob[0].onStartCommand();
        return Result.SUCCESS;
    }

    private void downloadServiceJobInstanceIsReady() {
        synchronized (lock) {
            LLog.v("Ferran, Download service job instance is ready now");
            lock.notifyAll();
        }
    }

    private void waitForDownloadServiceJobInstanceToBeReady() {
        synchronized (lock) {
            try {
                LLog.v("Ferran, Waiting for download service job instance to be ready");
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void scheduleJob() {
        LLog.v("Ferran, scheduling a job to start immediately in 1s");
        scheduleJob(EXECUTION_START_MILLIS);
    }

    private static void scheduleJob(final long startMillis) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                new JobRequest.Builder(TAG)
                        .setExecutionWindow(startMillis, EXECUTION_END_MILLIS)
                        .setBackoffCriteria(BACKOFF_MILLIS, JobRequest.BackoffPolicy.LINEAR)
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
