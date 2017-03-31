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

    public DownloadUpdater(DownloadHandler downloadHandler,
                           ExecutorService executor,
                           Pauser pauser,
                           DownloadCheck downloadCheck) {
        this.downloadHandler = downloadHandler;
        this.executor = executor;
        this.pauser = pauser;
        this.downloadCheck = downloadCheck;
    }

    public boolean update() {
        List<Download> allDownloads = downloadHandler.getAllDownloads();

        downloadHandler.deleteMarkedBatchesFor(allDownloads);
        updateFileSizeForFilesWithUnknownSize(allDownloads);

        boolean isCurrentlyDownloading = isCurrentlyDownloading(allDownloads);

        if (!isCurrentlyDownloading) {
            isCurrentlyDownloading = startNextQueuedDownload(allDownloads);
        }

        return isCurrentlyDownloading;
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
        downloadHandler.setDownloadSubmitted(download.getId());

        executor.submit(new DownloadTask(download.getId(), downloadHandler, pauser));
        Log.e(getClass().getSimpleName(), "Submitting to executor: " + download.getId() + " stage: " + download.getStage() + " hash: " + hashCode());
    }

    public void release() {
        executor.shutdown();
    }

}
