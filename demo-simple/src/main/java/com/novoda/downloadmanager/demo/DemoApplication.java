package com.novoda.downloadmanager.demo;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.facebook.stetho.Stetho;
import com.novoda.downloadmanager.DownloadManager;
import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.HttpClient;
import com.novoda.downloadmanager.StorageRequirementRuleFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class DemoApplication extends Application {

    private static final int TIMEOUT = 6;
    private static final int TWO_HUNDRED_MB_IN_BYTES = 209715200;
    private volatile DownloadManager downloadManager;
    private final DemoBatchSizeProvider batchSizeProvider = new DemoBatchSizeProvider();

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        createLiteDownloadManager();
    }

    private void createLiteDownloadManager() {
        Handler handler = new Handler(Looper.getMainLooper());

        downloadManager = DownloadManagerBuilder
                .newInstance(this, handler, R.mipmap.ic_launcher_round)
                .withCustomHttpClient(customHttpClient())
                .withLogHandle(new DemoLogHandle())
                .withStorageRequirementRules(StorageRequirementRuleFactory.createByteBasedRule(TWO_HUNDRED_MB_IN_BYTES))
                .withDownloadBatchRequirementRules(new DownloadBatchSizeRequirementRule(batchSizeProvider))
                .build();
    }

    private HttpClient customHttpClient() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .build();

        return new CustomHttpClient(okHttpClient);
    }

    public DownloadManager getDownloadManager() {
        return downloadManager;
    }

    public DemoBatchSizeProvider getBatchSizeProvider() {
        return batchSizeProvider;
    }
}
