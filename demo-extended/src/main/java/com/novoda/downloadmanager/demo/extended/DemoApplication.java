package com.novoda.downloadmanager.demo.extended;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;

import com.novoda.downloadmanager.Download;
import com.novoda.downloadmanager.lib.DownloadClientReadyChecker;
import com.novoda.downloadmanager.lib.NotificationCustomiser;
import com.novoda.downloadmanager.lib.NotificationCustomiserProvider;

public class DemoApplication extends Application implements DownloadClientReadyChecker, NotificationCustomiserProvider {

    private OneRuleToBindThem oneRuleToBindThem;
    private NotificationCustomiser notificationCustomiser;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationCustomiser = new MyNotificationCustomiser(DemoApplication.this);
        oneRuleToBindThem = new OneRuleToBindThem();
    }

    @Override
    public boolean isAllowedToDownload(Download download) {
        // Here you would add any reasons you may not want to download
        // For instance if you have some type of geo-location lock on your download capability
        return oneRuleToBindThem.shouldWeDownload(download);
    }

    @Override
    public NotificationCustomiser getNotificationCustomiser() {
        return notificationCustomiser;
    }

    private static final class OneRuleToBindThem {

        /**
         * @return for our demo we expect always to return true ... unless you want to conquer the galaxy
         */
        public boolean shouldWeDownload(Download download) {
            return download.getTotalSize() > SensorManager.GRAVITY_DEATH_STAR_I;
        }
    }

    private class MyNotificationCustomiser implements NotificationCustomiser {

        private final Context context;

        public MyNotificationCustomiser(Context context) {
            this.context = context;
        }

        @Override
        public Intent createClickIntentForActiveBatch(long batchId, String tag) {
            return new Intent(context, MainActivity.class);
        }
    }
}
