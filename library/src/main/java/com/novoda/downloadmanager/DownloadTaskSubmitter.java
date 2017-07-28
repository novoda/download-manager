package com.novoda.downloadmanager;

import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;

class DownloadTaskSubmitter {

    private final DownloadDatabaseWrapper downloadDatabaseWrapper;
    private final ExecutorService executor;
    private final Pauser pauser;
    private final DownloadCheck downloadCheck;

    public DownloadTaskSubmitter(DownloadDatabaseWrapper downloadDatabaseWrapper,
                                 ExecutorService executor,
                                 Pauser pauser,
                                 DownloadCheck downloadCheck) {
        this.downloadDatabaseWrapper = downloadDatabaseWrapper;
        this.executor = executor;
        this.pauser = pauser;
        this.downloadCheck = downloadCheck;
    }

    /**
     * @return true if a download is in progress
     */
    public boolean submitNextAvailableDownloadIfNotCurrentlyDownloading() {
        List<Download> allDownloads = downloadDatabaseWrapper.getAllDownloads();
        boolean isCurrentlyDownloading = isCurrentlyDownloading(allDownloads);

        if (!isCurrentlyDownloading) {
            isCurrentlyDownloading = startNextQueuedDownload(allDownloads);
        }

        return isCurrentlyDownloading;
    }

    private boolean isCurrentlyDownloading(List<Download> allDownloads) {
        for (Download download : allDownloads) {
            if (isActive(download)) {
                return true;
            }
        }
        return false;
    }

    private boolean isActive(Download download) {
        return download.getStage() == DownloadStage.SUBMITTED || download.getStage() == DownloadStage.RUNNING;
    }

    private boolean startNextQueuedDownload(List<Download> allDownloads) {
        for (Download download : allDownloads) {

            if (download.getStage() != DownloadStage.QUEUED) {
                continue;
            }

            ClientCheckResult clientCheckResult = downloadCheck.isAllowedToDownload(download);
            if (clientCheckResult.isAllowed()) {
                start(download);
                return true;
            }
            // TODO: send broadcast or fire callback to alert that a startDownload was denied
        }
        return false;
    }

    private void start(final Download download) {
        downloadDatabaseWrapper.setDownloadSubmitted(download.getId());

        executor.submit(new DownloadTask(download.getId(), downloadDatabaseWrapper, pauser));
        Log.e(getClass().getSimpleName(), "Submitting to executor: " + download.getId() + " stage: " + download.getStage() + " hash: " + hashCode());
    }

    public void stopSubmittingDownloadTasks() {
        executor.shutdown();
    }

}
