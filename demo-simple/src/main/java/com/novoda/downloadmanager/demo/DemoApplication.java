package com.novoda.downloadmanager.demo;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.facebook.stetho.Stetho;
import com.novoda.downloadmanager.DownloadManager;
import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.StorageRequirementRuleFactory;

public class DemoApplication extends Application {

    private static final int TWO_HUNDRED_MB_IN_BYTES = 209715200;
    private volatile DownloadManager downloadManager;

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
                .withLogHandle(new DemoLogHandle())
                .withStorageRequirementRules(StorageRequirementRuleFactory.createByteBasedRule(TWO_HUNDRED_MB_IN_BYTES))
                .build();
    }

    public DownloadManager getDownloadManager() {
        return downloadManager;
    }
}
