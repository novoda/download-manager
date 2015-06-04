package com.novoda.downloadmanager.lib;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.novoda.notils.logger.simple.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class DownloadExecutorFactory {
    private static final int KEEP_ALIVE_TIME = 10;
    private static final int DEFAULT_MAX_CONCURRENT_DOWNLOADS = 5;
    private static final String METADATA_MAX_CONCURRENT_DOWNLOADS = "com.novoda.downloadmanager.MaxConcurrentDownloads";

    public ExecutorService createExecutor(Context context) {
        int maxConcurrentDownloads = getMaximumConcurrentDownloads(context);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                maxConcurrentDownloads,
                maxConcurrentDownloads,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    private int getMaximumConcurrentDownloads(Context context) {
        try {
            String packageName = context.getApplicationContext().getPackageName();
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            Bundle bundle = applicationInfo.metaData;
            if (bundle == null) {
                return DEFAULT_MAX_CONCURRENT_DOWNLOADS;
            }

            return bundle.getInt(METADATA_MAX_CONCURRENT_DOWNLOADS, DEFAULT_MAX_CONCURRENT_DOWNLOADS);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Meta data value not found: " + e.getMessage());
        }
        return DEFAULT_MAX_CONCURRENT_DOWNLOADS;
    }
}
