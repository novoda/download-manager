package com.novoda.downloadmanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.novoda.downloadmanager.client.DownloadCheck;
import com.novoda.downloadmanager.client.GlobalClientCheck;

public class DownloadServiceConnection {

    private final Context context;
    private final GlobalClientCheck globalClientChecker;
    private final DownloadCheck downloadChecker;

    public DownloadServiceConnection(Context context, GlobalClientCheck globalClientChecker, DownloadCheck downloadCheck) {
        this.context = context;
        this.globalClientChecker = globalClientChecker;
        this.downloadChecker = downloadCheck;
    }

    public void startService() {
        Intent serviceIntent = new Intent(context, Service.class);
        context.bindService(serviceIntent, serviceConnection, Service.BIND_AUTO_CREATE);
    }

    public void stopService() {
        context.unbindService(serviceConnection);
        context.stopService(new Intent(context, Service.class));
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Service.DownloadServiceBinder downloadServiceBinder = (Service.DownloadServiceBinder) binder;
            downloadServiceBinder.setGlobalChecker(globalClientChecker);
            downloadServiceBinder.setDownloadChecker(downloadChecker);
            downloadServiceBinder.setServiceConnection(DownloadServiceConnection.this);
            downloadServiceBinder.fooStart();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // do nothing
        }
    };

}
