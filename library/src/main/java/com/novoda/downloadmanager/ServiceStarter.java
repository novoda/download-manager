package com.novoda.downloadmanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.novoda.downloadmanager.client.DownloadCheck;
import com.novoda.downloadmanager.client.GlobalClientCheck;

class ServiceStarter {

    private final Context context;

    private final GlobalClientCheck globalClientChecker;
    private final DownloadCheck downloadChecker;

    public ServiceStarter(Context context, GlobalClientCheck globalClientChecker, DownloadCheck downloadCheck) {
        this.context = context;
        this.globalClientChecker = globalClientChecker;
        this.downloadChecker = downloadCheck;
    }

    public void start() {
        Intent service = new Intent(context, Service.class);

        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                Service.ServiceBinder service = (Service.ServiceBinder) binder;
                service.setGlobalChecker(globalClientChecker);
                service.setDownloadChecker(downloadChecker);
                service.start();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                // do nothing
            }
        };
        context.bindService(service, serviceConnection, Service.BIND_AUTO_CREATE);
    }

}
