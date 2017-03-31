package com.novoda.downloadmanager.service;

import android.util.Log;

import com.novoda.downloadmanager.Pauser;
import com.novoda.downloadmanager.client.ClientCheckResult;
import com.novoda.downloadmanager.client.DownloadCheck;
import com.novoda.downloadmanager.domain.Download;
import com.novoda.downloadmanager.domain.DownloadFile;
import com.novoda.downloadmanager.domain.DownloadStage;
import com.novoda.downloadmanager.download.DownloadHandler;
import com.novoda.downloadmanager.download.task.DownloadTask;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class DownloadUpdater {

    private final DownloadHandler downloadHandler;
    private final ExecutorService executor;
    private final Pauser pauser;
    private final DownloadCheck downloadCheck;
    private final SubmittedDownloadsTracker tracker;

    public DownloadUpdater(DownloadHandler downloadHandler,
                           ExecutorService executor,
                           Pauser pauser,
                           DownloadCheck downloadCheck,
                           SubmittedDownloadsTracker tracker) {
        this.downloadHandler = downloadHandler;
        this.executor = executor;
        this.pauser = pauser;
        this.downloadCheck = downloadCheck;
        this.tracker = tracker;
    }

    public boolean update() {
        List<Download> allDownloads = downloadHandler.getAllDownloads();
        updateFileSizeForFilesWithUnknownSize(allDownloads);

        boolean downloadInProgress = hasActiveDownload(allDownloads); // if any startDownload is in the SUBMITTED (not RUNNING, but due to) or RUNNING state

        if (!downloadInProgress) {
            downloadInProgress = startNextQueuedDownload(allDownloads);
        }

        downloadHandler.deleteMarkedBatchesFor(allDownloads);
//        updateUserVisibleNotification(downloadBatches);

        // todo update notification

        return downloadInProgress;
    }

    private void updateFileSizeForFilesWithUnknownSize(List<Download> allDownloads) {
        for (Download download : allDownloads) {
            List<DownloadFile> files = download.getFiles();
            for (DownloadFile file : files) {
                if (hasUnknownTotalBytes(file)) {
                    downloadHandler.updateFileSize(file);
                }
            }
        }
    }

    private boolean hasUnknownTotalBytes(DownloadFile file) {
        return file.totalSize() == -1;
    }

    private boolean hasActiveDownload(List<Download> allDownloads) {
        for (Download download : allDownloads) {
            if (isActive(download)) {
                return true;
            }
        }
        return false;
    }

    private boolean isActive(Download download) {
        return tracker.contains(download.getId()) || download.getStage() == DownloadStage.RUNNING;
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
        if (tracker.contains(download.getId())) {
            return;
        }

        tracker.addDownloadId(download.getId());

        executor.submit(new DownloadTask(download.getId(), downloadHandler, pauser));
        Log.e(getClass().getSimpleName(), "Submitting to executor: " + download.getId() + " stage: " + download.getStage() + " hash: " + hashCode());
    }

    public void release() {
        executor.shutdown();
    }

}
