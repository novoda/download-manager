package com.novoda.downloadmanager;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;

class LiteJobDownload extends Job {

    private final DownloadManager downloadManager;

    LiteJobDownload(DownloadManager downloadManager) {
        this.downloadManager = downloadManager;
    }

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        downloadManager.submitAllStoredDownloads(() -> Logger.v("LiteJobDownload all jobs submitted"));
        Logger.v("LiteJobDownload run network recovery job");
        return Result.SUCCESS;
    }
}
