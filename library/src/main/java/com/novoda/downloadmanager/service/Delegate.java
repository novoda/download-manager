package com.novoda.downloadmanager.service;

import com.novoda.downloadmanager.DownloadServiceConnection;
import com.novoda.downloadmanager.Service;
import com.novoda.downloadmanager.client.ClientCheckResult;
import com.novoda.downloadmanager.client.GlobalClientCheck;
import com.novoda.downloadmanager.download.DownloadDatabaseWrapper;

public class Delegate {

    private final DownloadObserver downloadObserver;
    private final DownloadTaskSubmitter downloadTaskSubmitter;
    private final Service service;
    private final UpdateScheduler updateScheduler;
    private final GlobalClientCheck globalClientCheck;
    private final DownloadDatabaseWrapper downloadDatabaseWrapper;
    private final DownloadServiceConnection downloadServiceConnection;
    private final TotalFileSizeUpdater totalFileSizeUpdater;

    Delegate(DownloadObserver downloadObserver,
             DownloadTaskSubmitter downloadTaskSubmitter,
             Service service,
             UpdateScheduler updateScheduler,
             GlobalClientCheck globalClientCheck,
             DownloadDatabaseWrapper downloadDatabaseWrapper,
             DownloadServiceConnection downloadServiceConnection,
             TotalFileSizeUpdater totalFileSizeUpdater) {
        this.downloadObserver = downloadObserver;
        this.downloadTaskSubmitter = downloadTaskSubmitter;
        this.service = service;
        this.updateScheduler = updateScheduler;
        this.globalClientCheck = globalClientCheck;
        this.downloadDatabaseWrapper = downloadDatabaseWrapper;
        this.downloadServiceConnection = downloadServiceConnection;
        this.totalFileSizeUpdater = totalFileSizeUpdater;
    }

    public void onServiceStart() {
        ClientCheckResult clientCheckResult = globalClientCheck.onGlobalCheck();

        if (clientCheckResult.isAllowed()) {
            downloadObserver.startMonitoringDownloadChanges(onDownloadsTableUpdated);

            updateScheduler.scheduleNow(updateCallback);
        } else {
            service.stopSelf();
        }
    }

    private final DownloadObserver.Callback onDownloadsTableUpdated = new DownloadObserver.Callback() {
        @Override
        public void onDownloadsTableUpdated() {
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
            stopService();
        }
    }

    private boolean update() {
        downloadDatabaseWrapper.deleteAllDownloadsMarkedForDeletion();
        totalFileSizeUpdater.updateMissingTotalFileSizes();
        return downloadTaskSubmitter.submitNextAvailableDownloadIfNotCurrentlyDownloading();
    }

    public void revertSubmittedDownloadsToQueuedDownloads() {
        downloadDatabaseWrapper.revertSubmittedDownloadsToQueuedDownloads();
    }

    public void stopService() {
        release();
        downloadServiceConnection.stopService();
    }

    public void onDestroy() {
        release();
    }

    private void release() {
        downloadObserver.release();
        updateScheduler.release();
        downloadTaskSubmitter.stopSubmittingDownloadTasks();
    }

}
