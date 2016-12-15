package com.novoda.downloadmanager;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.novoda.downloadmanager.client.ClientCheckResult;
import com.novoda.downloadmanager.client.DownloadCheck;
import com.novoda.downloadmanager.client.GlobalClientCheck;
import com.novoda.downloadmanager.download.DownloadHandler;
import com.novoda.downloadmanager.download.DownloadHandlerCreator;
import com.novoda.notils.caster.Classes;

import java.util.concurrent.ExecutorService;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

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

        DownloadUpdater downloadUpdater = createDownloadUpdater(executor, null);

        delegate = new Delegate(updateThread, updateHandler, executor, downloadUpdater, getContentResolver(), this);

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

    private static class Delegate {

        private final HandlerThread updateThread;
        private final Handler updateHandler;
        private final ExecutorService executor;
        private final DownloadUpdater downloadUpdater;
        private final ContentResolver contentResolver;
        private final Service service;

        private ContentObserver contentObserver;

        private Delegate(HandlerThread updateThread,
                         Handler updateHandler,
                         ExecutorService executor,
                         DownloadUpdater downloadUpdater,
                         ContentResolver contentResolver,
                         Service service) {
            this.updateThread = updateThread;
            this.updateHandler = updateHandler;
            this.executor = executor;
            this.downloadUpdater = downloadUpdater;
            this.contentResolver = contentResolver;
            this.service = service;
        }

        public void onStartCommand(Intent intent, int flags, int startId) {
            GlobalClientCheck globalClientCheck = Classes.from(intent.getSerializableExtra("foo"));
            DownloadCheck downloadCheck = Classes.from(intent.getSerializableExtra("bar"));
            ClientCheckResult clientCheckResult = globalClientCheck.onGlobalCheck();

            if (clientCheckResult.isAllowed()) {
                startMonitoringDownloadChanges();

                updateHandler.post(updateCallback);
            } else {
                service.stopSelf();
            }
        }

        private void startMonitoringDownloadChanges() {
            contentObserver = new ContentObserver(updateHandler) {
                @Override
                public void onChange(boolean selfChange) {
                    update();
                }
            };
            contentResolver.registerContentObserver(Provider.DOWNLOAD_SERVICE_UPDATE, true, contentObserver);
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

        public void shutDown() {
            Log.e("!!!", "shutting down service");
            contentResolver.unregisterContentObserver(contentObserver);
            executor.shutdownNow();
            updateThread.quit();
            service.stopSelf();
        }

    }

}
