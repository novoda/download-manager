package com.novoda.downloadmanager.service;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class DownloadExecutorFactory {

    private static final int KEEP_ALIVE_TIME = 10;

    public ThreadPoolExecutor createExecutor() {
        int maxConcurrentDownloads = 1;
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