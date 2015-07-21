package com.novoda.downloadmanager.demo.extended;

import android.app.Application;
import android.content.Context;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.novoda.downloadmanager.Download;
import com.novoda.downloadmanager.lib.DownloadClientReadyChecker;
import com.novoda.notils.logger.simple.Log;

public class DemoApplication extends Application implements DownloadClientReadyChecker {

    private OneRuleToBindThem oneRuleToBindThem;

    @Override
    public void onCreate() {
        super.onCreate();
        oneRuleToBindThem = new OneRuleToBindThem();
        Log.setShowLogs(true);
    }

    @Override
    public boolean isAllowedToDownload(Download download) {
        // Here you would add any reasons you may not want to download
        // For instance if you have some type of geo-location lock on your download capability
        //return oneRuleToBindThem.shouldWeDownload(download);
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return networkInfo != null && networkInfo.isConnected();
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
