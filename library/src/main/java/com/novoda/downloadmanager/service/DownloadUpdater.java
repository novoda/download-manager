package com.novoda.downloadmanager.service;

import android.util.Log;

import com.novoda.downloadmanager.Pauser;
import com.novoda.downloadmanager.client.ClientCheckResult;
import com.novoda.downloadmanager.client.DownloadCheck;
import com.novoda.downloadmanager.domain.Download;
import com.novoda.downloadmanager.domain.DownloadFile;
import com.novoda.downloadmanager.domain.DownloadStage;
import com.novoda.downloadmanager.download.ContentLengthFetcher;
import com.novoda.downloadmanager.download.DownloadDatabaseWrapper;
import com.novoda.downloadmanager.download.task.DownloadTask;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class DownloadUpdater {

    private final DownloadDatabaseWrapper downloadDatabaseWrapper;
    private final ExecutorService executor;
    private final Pauser pauser;
    private final DownloadCheck downloadCheck;
    private final ContentLengthFetcher contentLengthFetcher;

    public DownloadUpdater(DownloadDatabaseWrapper downloadDatabaseWrapper,
                           ExecutorService executor,
                           Pauser pauser,
                           DownloadCheck downloadCheck,
                           ContentLengthFetcher contentLengthFetcher) {
        this.downloadDatabaseWrapper = downloadDatabaseWrapper;
        this.executor = executor;
        this.pauser = pauser;
        this.downloadCheck = downloadCheck;
        this.contentLengthFetcher = contentLengthFetcher;
    }

    public boolean update() {
        List<Download> allDownloads = downloadDatabaseWrapper.getAllDownloads();

        downloadDatabaseWrapper.deleteAllDownloadsMarkedForDeletion();
        updateFileSizeForFilesWithUnknownSize();

        boolean isCurrentlyDownloading = isCurrentlyDownloading(allDownloads);

        if (!isCurrentlyDownloading) {
            isCurrentlyDownloading = startNextQueuedDownload(allDownloads);
        }

        return isCurrentlyDownloading;
    }

    private void updateFileSizeForFilesWithUnknownSize() {
        List<DownloadFile> files = downloadDatabaseWrapper.getFilesWithUnknownTotalSize();
        for (DownloadFile file : files) {
            long totalBytes = contentLengthFetcher.fetchContentLengthFor(file);
            downloadDatabaseWrapper.updateFileSize(file, totalBytes);
        }
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

    public void release() {
        executor.shutdown();
    }

}
