package com.novoda.downloadmanager.demo.simple;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.novoda.downloadmanager.lib.DownloadManagerModules;
import com.novoda.downloadmanager.notifications.NotificationChannelProvider;

public class DemoApplication extends Application implements DownloadManagerModules.Provider {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public DownloadManagerModules provideDownloadManagerModules() {
        return DownloadManagerModules.Builder.from(this)
                .withNotificationManagerCustomiser(new AndroidONotificationChannelProvider())
                .build();
    }

    private static class AndroidONotificationChannelProvider implements NotificationChannelProvider {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public NotificationChannel getNotificationChannel() {
            return new NotificationChannel("app id", "Beard Downloader", NotificationManager.IMPORTANCE_DEFAULT);
        }
    }
}
