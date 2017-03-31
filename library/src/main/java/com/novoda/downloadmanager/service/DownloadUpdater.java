package com.novoda.downloadmanager.service;

import com.novoda.downloadmanager.Pauser;
import com.novoda.downloadmanager.client.ClientCheckResult;
import com.novoda.downloadmanager.client.DownloadCheck;
import com.novoda.downloadmanager.domain.Download;
import com.novoda.downloadmanager.domain.DownloadFile;
import com.novoda.downloadmanager.domain.DownloadId;
import com.novoda.downloadmanager.domain.DownloadStage;
import com.novoda.downloadmanager.download.DownloadHandler;
import com.novoda.downloadmanager.download.task.DownloadTask;

import java.util.List;
import java.util.concurrent.ExecutorService;

class DownloadUpdater {

    private final DownloadHandler downloadHandler;
    private final ExecutorService executor;
    private final Pauser pauser;
    private final DownloadCheck downloadCheck;

    DownloadUpdater(DownloadHandler downloadHandler, ExecutorService executor, Pauser pauser, DownloadCheck downloadCheck) {
        this.downloadHandler = downloadHandler;
        this.executor = executor;
        this.pauser = pauser;
        this.downloadCheck = downloadCheck;
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
        return download.getStage() == DownloadStage.SUBMITTED || download.getStage() == DownloadStage.RUNNING;
    }

    private boolean startNextQueuedDownload(List<Download> allDownloads) {
        for (Download download : allDownloads) {

            if (download.getStage() != DownloadStage.QUEUED) {
                continue;
            }

            ClientCheckResult clientCheckResult = downloadCheck.isAllowedToDownload(download);
            if (clientCheckResult.isAllowed()) {
                startDownload(download.getId());
                return true;
            }
            // TODO: send broadcast or fire callback to alert that a startDownload was denied
        }
        return false;
    }

    private void startDownload(final DownloadId downloadId) {
        downloadHandler.setDownloadSubmitted(downloadId);
        executor.submit(new DownloadTask(downloadId, downloadHandler, pauser));
    }

    public void release() {
        executor.shutdown();
    }

}
