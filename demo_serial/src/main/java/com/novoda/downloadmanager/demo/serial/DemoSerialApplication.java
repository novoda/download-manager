package com.novoda.downloadmanager.demo.serial;

import android.app.Application;

import com.novoda.downloadmanager.lib.DownloadClientReadyChecker;

public class DemoSerialApplication extends Application implements DownloadClientReadyChecker {
    @Override
    public boolean isReadyToDownload() {
        // Here you would add any reasons you may not want to download
        // For instance if you have some type of geo-location lock on your download capability
        return true;
    }
}
