package com.novoda.downloadmanager;

class Delegate {

    private final DownloadObserver downloadObserver;
    private final DownloadTaskSubmitter downloadTaskSubmitter;
    private final Timer timer;
    private final GlobalClientCheck globalClientCheck;
    private final DownloadDatabaseWrapper downloadDatabaseWrapper;
    private final DownloadServiceConnection downloadServiceConnection;
    private final TotalFileSizeUpdater totalFileSizeUpdater;

    Delegate(DownloadObserver downloadObserver,
             DownloadTaskSubmitter downloadTaskSubmitter,
             Timer timer,
             GlobalClientCheck globalClientCheck,
             DownloadDatabaseWrapper downloadDatabaseWrapper,
             DownloadServiceConnection downloadServiceConnection,
             TotalFileSizeUpdater totalFileSizeUpdater) {
        this.downloadObserver = downloadObserver;
        this.downloadTaskSubmitter = downloadTaskSubmitter;
        this.timer = timer;
        this.globalClientCheck = globalClientCheck;
        this.downloadDatabaseWrapper = downloadDatabaseWrapper;
        this.downloadServiceConnection = downloadServiceConnection;
        this.totalFileSizeUpdater = totalFileSizeUpdater;
    }

    public void onServiceStart() {
        ClientCheckResult clientCheckResult = globalClientCheck.onGlobalCheck();

        if (clientCheckResult.isAllowed()) {
            downloadObserver.startMonitoringDownloadChanges(onDownloadsTableUpdated);
            timer.scheduleNow(updateCallback);
        } else {
            stopService();
        }
    }

    private final DownloadObserver.Callback onDownloadsTableUpdated = new DownloadObserver.Callback() {
        @Override
        public void onDownloadsTableUpdated() {
            continueOrShutdown();
        }
    };

    private final Timer.Callback updateCallback = new Timer.Callback() {
        @Override
        public void onUpdate() {
            continueOrShutdown();
        }
    };

    private void continueOrShutdown() {
        downloadDatabaseWrapper.deleteAllDownloadsMarkedForDeletion();
        totalFileSizeUpdater.updateMissingTotalFileSizes();
        boolean downloadInProgress = downloadTaskSubmitter.submitNextAvailableDownloadIfNotCurrentlyDownloading();

        if (downloadInProgress) {
            timer.scheduleLater(updateCallback);
        } else {
            stopService();
        }
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
        timer.release();
        downloadTaskSubmitter.stopSubmittingDownloadTasks();
    }

}
