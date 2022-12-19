package com.novoda.downloadmanager;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class LiteJobDownload extends Worker {

    private final DownloadManager downloadManager;

    public static WorkRequest newInstance(String tag, Constraints constraints) {
        return new OneTimeWorkRequest.Builder(LiteJobDownload.class)
                .addTag(tag)
                .setConstraints(constraints)
                .build();
    }

    LiteJobDownload(DownloadManager downloadManager, Context context, WorkerParameters params) {
        super(context, params);
        this.downloadManager = downloadManager;
    }

    @NonNull
    @Override
    public Result doWork() {
        downloadManager.submitAllStoredDownloads(() -> Logger.v("LiteJobDownload all jobs submitted"));
        Logger.v("LiteJobDownload run network recovery job");
        return Result.success();
    }
}
