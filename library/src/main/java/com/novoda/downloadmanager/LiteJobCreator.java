package com.novoda.downloadmanager;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.ListenableWorker;
import androidx.work.WorkerFactory;
import androidx.work.WorkerParameters;

class LiteJobCreator extends WorkerFactory {

    static final String TAG = "download-manager-reschedule";

    private final DownloadManager downloadManager;

    LiteJobCreator(DownloadManager downloadManager) {
        this.downloadManager = downloadManager;
    }

    @Nullable @Override
    public ListenableWorker createWorker(@NonNull Context appContext, @NonNull String workerClassName, @NonNull WorkerParameters workerParameters) {
        if (LiteJobDownload.class.getName().equals(workerClassName)) {
            return new LiteJobDownload(downloadManager, appContext, workerParameters);
        } else {
            return null;
        }
    }
}
