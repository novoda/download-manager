package com.novoda.downloadmanager;

import android.content.Context;

import com.novoda.downloadmanager.client.DownloadCheck;
import com.novoda.downloadmanager.client.GlobalClientCheck;

class ServiceBuilder {

    private GlobalClientCheck globalClientCheck = GlobalClientCheck.IGNORED;
    private DownloadCheck downloadCheck = DownloadCheck.IGNORED;

    public ServiceBuilder with(GlobalClientCheck globalClientCheck) {
        this.globalClientCheck = globalClientCheck;
        return this;
    }

    public ServiceBuilder with(DownloadCheck downloadCheck) {
        this.downloadCheck = downloadCheck;
        return this;
    }

    public DownloadServiceConnection build(Context context) {
        return new DownloadServiceConnection(context, globalClientCheck, downloadCheck);
    }

}
