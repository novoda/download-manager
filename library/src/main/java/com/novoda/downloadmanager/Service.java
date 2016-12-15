package com.novoda.downloadmanager;

import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.novoda.downloadmanager.client.DownloadCheck;
import com.novoda.downloadmanager.download.DownloadHandler;
import com.novoda.downloadmanager.download.DownloadHandlerCreator;

import java.util.concurrent.ExecutorService;

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
        DownloadExecutorFactory factory = new DownloadExecutorFactory();
        ExecutorService executor = factory.createExecutor();

        HandlerThread updateThread = new HandlerThread("DownloadManager-UpdateThread");
        updateThread.start();
        Handler updateHandler = new Handler(updateThread.getLooper());
        UpdateScheduler updateScheduler = new UpdateScheduler(updateThread, updateHandler);

        DownloadUpdater downloadUpdater = createDownloadUpdater(executor, null);
        DownloadObserver downloadObserver = new DownloadObserver(updateHandler, getContentResolver());
        delegate = new Delegate(downloadObserver, downloadUpdater, this, updateScheduler);

    }

    private DownloadUpdater createDownloadUpdater(ExecutorService executor, DownloadCheck downloadCheck) {
        DownloadHandler downloadHandler = DownloadHandlerCreator.create(getContentResolver());
        Pauser pauser = new Pauser(LocalBroadcastManager.getInstance(this));
        return new DownloadUpdater(downloadHandler, executor, pauser, downloadCheck);
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
