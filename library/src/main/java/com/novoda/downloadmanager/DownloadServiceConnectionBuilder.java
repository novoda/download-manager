package com.novoda.downloadmanager;

import android.content.Context;

class DownloadServiceConnectionBuilder {

    private GlobalClientCheck globalClientCheck = GlobalClientCheck.IGNORED;
    private DownloadCheck downloadCheck = DownloadCheck.IGNORED;

    public DownloadServiceConnectionBuilder with(GlobalClientCheck globalClientCheck) {
        this.globalClientCheck = globalClientCheck;
        return this;
    }

    public DownloadServiceConnectionBuilder with(DownloadCheck downloadCheck) {
        this.downloadCheck = downloadCheck;
        return this;
    }

    public DownloadServiceConnection build(Context context) {
        return new DownloadServiceConnection(context, globalClientCheck, downloadCheck);
    }

}
