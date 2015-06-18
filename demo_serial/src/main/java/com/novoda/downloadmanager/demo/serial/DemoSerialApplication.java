package com.novoda.downloadmanager.demo.serial;

import android.app.Application;

import com.novoda.downloadmanager.lib.DownloadClientReadyChecker;

public class DemoSerialApplication extends Application implements DownloadClientReadyChecker {
    @Override
    public boolean isReadyToDownload() {
        return true;
    }
}
