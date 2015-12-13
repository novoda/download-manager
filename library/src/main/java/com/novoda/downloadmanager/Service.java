package com.novoda.downloadmanager;

import android.content.Intent;
import android.database.ContentObserver;
import android.os.*;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.novoda.downloadmanager.client.DownloadCheck;
import com.novoda.downloadmanager.client.GlobalClientCheck;
import com.novoda.notils.caster.Classes;
import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.ExecutorService;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

public class Service extends android.app.Service {

    private HandlerThread updateThread;
    private Handler updateHandler;
    private ExecutorService executor;
    private DownloadUpdater downloadUpdater;

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
        executor = factory.createExecutor();
    }

    private final ContentObserver contentObserver = new ContentObserver(updateHandler) {
        @Override
        public void onChange(boolean selfChange) {
            update();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        GlobalClientCheck globalClientCheck = Classes.from(intent.getSerializableExtra("foo"));
        DownloadCheck downloadCheck = Classes.from(intent.getSerializableExtra("bar"));
        ClientCheckResult clientCheckResult = globalClientCheck.onGlobalCheck();

        if (clientCheckResult.isAllowed()) {
            downloadUpdater = createDownloadUpdater(downloadCheck);
            startMonitoringDownloadChanges();

            updateHandler.post(updateCallback);
        } else {
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private DownloadUpdater createDownloadUpdater(DownloadCheck downloadCheck) {
        ContentLengthFetcher contentLengthFetcher = new ContentLengthFetcher(new OkHttpClient());
        DatabaseInteraction databaseInteraction = new DatabaseInteraction(getContentResolver());
        DownloadHandler downloadHandler = new DownloadHandler(databaseInteraction, contentLengthFetcher);
        Pauser pauser = new Pauser(LocalBroadcastManager.getInstance(this));
        return new DownloadUpdater(downloadHandler, executor, pauser, downloadCheck);
    }

    private void startMonitoringDownloadChanges() {
        updateThread = new HandlerThread("DownloadManager-UpdateThread");
        updateThread.start();
        updateHandler = new Handler(updateThread.getLooper());
        getContentResolver().registerContentObserver(Provider.DOWNLOAD_SERVICE_UPDATE, true, contentObserver);
    }

    private final Runnable updateCallback = new Runnable() {
        @Override
        public void run() {
            Log.e("!!!", "update callback triggered");
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            boolean isActive = update();
            if (isActive) {
                scheduleUpdate();
            } else {
                shutDown();
            }
        }
    };

    private void scheduleUpdate() {
        updateHandler.removeCallbacks(updateCallback);
        updateHandler.postDelayed(updateCallback, 5 * MINUTE_IN_MILLIS);
    }

    private boolean update() {
        Log.e("!!!", "update!");
        return downloadUpdater.update();
    }

    @Override
    public void onDestroy() {
        shutDown();
        super.onDestroy();
    }

    private void shutDown() {
        Log.e("!!!", "shutting down service");
        getContentResolver().unregisterContentObserver(contentObserver);
        executor.shutdownNow();
        updateThread.quit();
        stopSelf();
    }

}
