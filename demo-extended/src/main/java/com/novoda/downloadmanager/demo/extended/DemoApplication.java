package com.novoda.downloadmanager.demo.extended;

import android.app.Application;
import android.hardware.SensorManager;

import com.novoda.downloadmanager.Download;
import com.novoda.downloadmanager.lib.DownloadManagerModules;
import com.novoda.downloadmanager.lib.DownloadClientReadyChecker;

public class DemoApplication extends Application implements DownloadManagerModules.Provider {

    private DemoNotificationCustomiser notificationCustomiser;
    private OneRuleToBindThem oneRuleToBindThem;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationCustomiser = new DemoNotificationCustomiser(DemoApplication.this);
        oneRuleToBindThem = new OneRuleToBindThem();
    }

    @Override
    public DownloadManagerModules provideDownloadManagerModules() {
        return DownloadManagerModules.Builder.from(this)
                .withQueuedNotificationCustomiser(notificationCustomiser)
                .withDownloadingNotificationCustomiser(notificationCustomiser)
                .withDownloadClientReadyChecker(oneRuleToBindThem)
                .build();
    }

    private static final class OneRuleToBindThem implements DownloadClientReadyChecker {

        /**
         * @return for our demo we expect always to return true ... unless you want to conquer the galaxy
         */
        @Override
        public boolean isAllowedToDownload(Download download) {
            // Here you would add any reasons you may not want to download
            // For instance if you have some type of geo-location lock on your download capability
            return download.getTotalSize() > SensorManager.GRAVITY_DEATH_STAR_I;
        }
    }

}
