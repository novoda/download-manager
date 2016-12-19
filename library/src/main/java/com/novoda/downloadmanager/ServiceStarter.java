package com.novoda.downloadmanager;

import android.content.Context;
import android.content.Intent;

import com.novoda.downloadmanager.client.DownloadCheck;
import com.novoda.downloadmanager.client.GlobalClientCheck;

class ServiceStarter {

    static final String EXTRA_GLOBAL_CLIENT_CHECKER = "foo";
    static final String EXTRA_DOWNLOAD_CHECKER = "bar";

    private final Context context;

    private final GlobalClientCheck globalClientCheck;
    private final DownloadCheck downloadCheck;

    public ServiceStarter(Context context, GlobalClientCheck globalClientCheck, DownloadCheck downloadCheck) {
        this.context = context;
        this.globalClientCheck = globalClientCheck;
        this.downloadCheck = downloadCheck;
    }

    public void start() {
        Intent service = new Intent(context, Service.class);

        service.putExtra(EXTRA_GLOBAL_CLIENT_CHECKER, globalClientCheck);
        service.putExtra(EXTRA_DOWNLOAD_CHECKER, downloadCheck);

        context.startService(service);
    }

}
