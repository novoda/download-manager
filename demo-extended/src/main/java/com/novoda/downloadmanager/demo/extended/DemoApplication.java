package com.novoda.downloadmanager.demo.extended;

import android.app.Application;
import android.hardware.SensorManager;

import com.novoda.downloadmanager.CancelledNotificationCustomiser;
import com.novoda.downloadmanager.CompleteNotificationCustomiser;
import com.novoda.downloadmanager.Download;
import com.novoda.downloadmanager.DownloadingNotificationCustomiser;
import com.novoda.downloadmanager.FailedNotificationCustomiser;
import com.novoda.downloadmanager.NotificationCustomiserProvider;
import com.novoda.downloadmanager.QueuedNotificationCustomiser;
import com.novoda.downloadmanager.lib.DownloadClientReadyChecker;

public class DemoApplication extends Application implements DownloadClientReadyChecker, NotificationCustomiserProvider {

    private OneRuleToBindThem oneRuleToBindThem;
    private DemoNotificationCustomiser notificationCustomiser;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationCustomiser = new DemoNotificationCustomiser(DemoApplication.this);
        oneRuleToBindThem = new OneRuleToBindThem();
    }

    @Override
    public boolean isAllowedToDownload(Download download) {
        // Here you would add any reasons you may not want to download
        // For instance if you have some type of geo-location lock on your download capability
        return oneRuleToBindThem.shouldWeDownload(download);
    }

    @Override
    public QueuedNotificationCustomiser getQueuedNotificationCustomiser() {
        return notificationCustomiser;
    }

    @Override
    public DownloadingNotificationCustomiser getDownloadingNotificationCustomiser() {
        return notificationCustomiser;
    }

    @Override
    public CompleteNotificationCustomiser getCompleteNotificationCustomiser() {
        return null;
    }

    @Override
    public CancelledNotificationCustomiser getCancelledNotificationCustomiser() {
        return null;
    }

    @Override
    public FailedNotificationCustomiser getFailedNotificationCustomiser() {
        return null;
    }

    private static final class OneRuleToBindThem {

        /**
         * @return for our demo we expect always to return true ... unless you want to conquer the galaxy
         */
        public boolean shouldWeDownload(Download download) {
            return download.getTotalSize() > SensorManager.GRAVITY_DEATH_STAR_I;
        }
    }

}
