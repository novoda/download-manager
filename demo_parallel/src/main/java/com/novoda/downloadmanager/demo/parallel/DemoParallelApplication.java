package com.novoda.downloadmanager.demo.parallel;

import android.app.Application;

import com.novoda.downloadmanager.lib.DownloadClientReadyChecker;

public class DemoParallelApplication extends Application implements DownloadClientReadyChecker {
    @Override
    public boolean isReadyToDownload() {
        return true;
    }
}
