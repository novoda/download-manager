package com.novoda.downloadmanager;

import androidx.annotation.NonNull;

import com.evernote.android.job.Job;

class LiteJobDownload extends Job {

    private final LiteDownloadManager liteDownloadManager;

    LiteJobDownload(LiteDownloadManager liteDownloadManager) {
        this.liteDownloadManager = liteDownloadManager;
    }

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        liteDownloadManager.submitAllStoredDownloads(() -> Logger.v("LiteJobDownload all jobs submitted"));
        Logger.v("LiteJobDownload run network recovery job");
        return Result.SUCCESS;
    }
}
