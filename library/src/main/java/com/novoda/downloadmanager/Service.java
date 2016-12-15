package com.novoda.downloadmanager;

import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.novoda.downloadmanager.service.Delegate;
import com.novoda.downloadmanager.service.DelegateCreator;

public class Service extends android.app.Service {

    private Delegate delegate;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("!!!", "service created");

        HandlerThread updateThread = new HandlerThread("DownloadManager-UpdateThread");
        updateThread.start();
        Handler updateHandler = new Handler(updateThread.getLooper());

        delegate = DelegateCreator.create(updateThread, updateHandler, this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        delegate.onStartCommand(intent, flags, startId);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        delegate.shutDown();
        super.onDestroy();
    }

}
