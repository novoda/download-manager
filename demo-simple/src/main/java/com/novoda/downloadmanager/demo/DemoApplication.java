package com.novoda.downloadmanager.demo;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.facebook.stetho.Stetho;
import com.novoda.downloadmanager.ByteBasedStorageRequirementRule;
import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.LiteDownloadManagerCommands;

public class DemoApplication extends Application {

    private static final int TWO_HUNDRED_MB_IN_BYTES = 200000000;
    private volatile LiteDownloadManagerCommands liteDownloadManagerCommands;

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        createLiteDownloadManager();
    }

    private void createLiteDownloadManager() {
        Handler handler = new Handler(Looper.getMainLooper());

        liteDownloadManagerCommands = DownloadManagerBuilder
                .newInstance(this, handler, R.mipmap.ic_launcher_round)
                .withLogHandle(new DemoLogHandle())
                .withStorageRequirementRules(ByteBasedStorageRequirementRule.withBytesOfStorageRemaining(TWO_HUNDRED_MB_IN_BYTES))
                .build();
    }

    public LiteDownloadManagerCommands getLiteDownloadManagerCommands() {
        return liteDownloadManagerCommands;
    }
}
