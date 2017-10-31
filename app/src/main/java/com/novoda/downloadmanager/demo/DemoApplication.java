package com.novoda.downloadmanager.demo;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.facebook.stetho.Stetho;
import com.novoda.downloadmanager.DownloadsPersistence;
import com.novoda.downloadmanager.FileDownloader;
import com.novoda.downloadmanager.FileSizeRequester;
import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.LiteDownloadManagerCommands;
import com.novoda.downloadmanager.NotificationCreator;

import java.util.concurrent.TimeUnit;

public class DemoApplication extends Application {

    private volatile LiteDownloadManagerCommands liteDownloadManagerCommands;

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        createLiteDownloadManager();
    }

    private void createLiteDownloadManager() {
        FileSizeRequester fileSizeRequester = new CustomFileSizeRequester();
        FileDownloader fileDownloader = new CustomFileDownloader();
        DownloadsPersistence downloadsPersistence = new CustomDownloadsPersistence();
        NotificationCreator notificationCreator = new CustomNotificationCreator(this, R.mipmap.ic_launcher_round);

        Handler handler = new Handler(Looper.getMainLooper());

        liteDownloadManagerCommands = DownloadManagerBuilder
                .newInstance(this, handler, R.mipmap.ic_launcher_round)
                .withFileDownloaderCustom(fileSizeRequester, fileDownloader)
                .withFilePersistenceExternal()
                .withFilePersistenceCustom(CustomFilePersistence.class)
                .withDownloadsPersistenceCustom(downloadsPersistence)
                .withNotification(notificationCreator)
                .withNetworkRecovery(false)
                .withCallbackThrottleCustom(CustomCallbackThrottle.class)
                .withCallbackThrottleByTime(TimeUnit.SECONDS, 3)
                .withCallbackThrottleByProgressIncrease()
                .build();
    }

    public LiteDownloadManagerCommands getLiteDownloadManagerCommands() {
        return liteDownloadManagerCommands;
    }
}
