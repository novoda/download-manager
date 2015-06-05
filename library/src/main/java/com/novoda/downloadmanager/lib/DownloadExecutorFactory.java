package com.novoda.downloadmanager.lib;

import android.content.Context;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class DownloadExecutorFactory {

    private static final int KEEP_ALIVE_TIME = 10;

    private final ConcurrentDownloadsLimitProvider concurrentDownloadsLimitProvider;

    static DownloadExecutorFactory newInstance(Context context) {
        return new DownloadExecutorFactory(ConcurrentDownloadsLimitProvider.newInstance(context));
    }

    DownloadExecutorFactory(ConcurrentDownloadsLimitProvider concurrentDownloadsLimitProvider) {
        this.concurrentDownloadsLimitProvider = concurrentDownloadsLimitProvider;
    }

    public ThreadPoolExecutor createExecutor() {
        int maxConcurrentDownloads = concurrentDownloadsLimitProvider.getConcurrentDownloadsLimit();
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
