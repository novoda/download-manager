package com.novoda.downloadmanager.service;

import android.app.Service;
import android.util.Log;

import com.novoda.downloadmanager.client.ClientCheckResult;
import com.novoda.downloadmanager.client.GlobalClientCheck;

public class Delegate {

    private final DownloadObserver downloadObserver;
    private final DownloadUpdater downloadUpdater;
    private final Service service;
    private final UpdateScheduler updateScheduler;
    private final GlobalClientCheck globalClientCheck;

    Delegate(DownloadObserver downloadObserver,
             DownloadUpdater downloadUpdater,
             Service service, UpdateScheduler updateScheduler,
             GlobalClientCheck globalClientCheck) {
        this.downloadObserver = downloadObserver;
        this.downloadUpdater = downloadUpdater;
        this.service = service;
        this.updateScheduler = updateScheduler;
        this.globalClientCheck = globalClientCheck;
    }

    public void start() {
        ClientCheckResult clientCheckResult = globalClientCheck.onGlobalCheck();

        if (clientCheckResult.isAllowed()) {
            downloadObserver.startMonitoringDownloadChanges(onDatabaseUpdate);

            updateScheduler.scheduleNow(updateCallback);
        } else {
            service.stopSelf();
        }
    }

    private final DownloadObserver.OnUpdate onDatabaseUpdate = new DownloadObserver.OnUpdate() {
        @Override
        public void onUpdate() {
            continueOrShutdown();
        }
    };

    private final UpdateScheduler.OnUpdate updateCallback = new UpdateScheduler.OnUpdate() {
        @Override
        public void onUpdate() {
            continueOrShutdown();
        }
    };

    private void continueOrShutdown() {
        boolean isActive = update();
        if (isActive) {
            updateScheduler.scheduleLater(updateCallback);
        } else {
            shutDown();
        }
    }

    private boolean update() {
        Log.e("!!!", "update!");
        return downloadUpdater.update();
    }

    public void shutDown() {
        Log.e("!!!", "shutting down service");
        downloadObserver.release();
        updateScheduler.release();
        downloadUpdater.release();
        service.stopSelf();
    }

}
