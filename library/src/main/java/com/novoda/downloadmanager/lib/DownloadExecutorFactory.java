package com.novoda.downloadmanager.lib;

import android.content.Context;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class DownloadExecutorFactory {

    private static final int KEEP_ALIVE_TIME = 10;

    private final int maxConcurrentDownloads;

    static DownloadExecutorFactory newInstance(Context context) {
        ConcurrentDownloadsLimitProvider concurrentDownloadsLimitProvider = ConcurrentDownloadsLimitProvider.newInstance(context);
        int maxConcurrentDownloads = concurrentDownloadsLimitProvider.getConcurrentDownloadsLimit();
        return new DownloadExecutorFactory(maxConcurrentDownloads);
    }

    DownloadExecutorFactory(int maxConcurrentDownloads) {
        this.maxConcurrentDownloads = maxConcurrentDownloads;
    }

    public ThreadPoolExecutor createExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                maxConcurrentDownloads,
                maxConcurrentDownloads,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

}
