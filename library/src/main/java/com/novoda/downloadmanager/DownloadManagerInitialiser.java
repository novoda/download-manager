package com.novoda.downloadmanager;

import android.content.Context;
import androidx.work.Configuration;
import androidx.work.WorkManager;

public class DownloadManagerInitialiser {

    public static void initialise(Context context, DownloadManager downloadManager) {
        Configuration configuration = new Configuration.Builder()
            .setWorkerFactory(createWorkerFactory(downloadManager))
            .build();
        WorkManager.initialize(context, configuration);
    }

    public static LiteJobCreator createWorkerFactory(DownloadManager downloadManager) {
        return new LiteJobCreator(downloadManager);
    }
}
