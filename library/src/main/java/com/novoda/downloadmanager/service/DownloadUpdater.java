package com.novoda.downloadmanager.service;

import android.util.Log;

import com.novoda.downloadmanager.Pauser;
import com.novoda.downloadmanager.client.ClientCheckResult;
import com.novoda.downloadmanager.client.DownloadCheck;
import com.novoda.downloadmanager.domain.Download;
import com.novoda.downloadmanager.domain.DownloadFile;
import com.novoda.downloadmanager.domain.DownloadId;
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
        Log.e("!!!", "update triggered");

        List<Download> allDownloads = downloadHandler.getAllDownloads();

        updateTotalBytesFor(allDownloads);

        boolean isActive = hasActiveDownload(allDownloads);

        Log.e("!!!", "are we active? " + isActive);

        if (!isActive) {
            isActive = triggerDownload(allDownloads);
        }

        downloadHandler.deleteMarkedBatchesFor(allDownloads);
//        updateUserVisibleNotification(downloadBatches);


        // todo update notification

        return isActive;
    }

    private boolean triggerDownload(List<Download> allDownloads) {
        for (Download download : allDownloads) {
            Log.e("!!!", "trigger download? : " + download.getId().toString());

            if (canSkipDownload(download)) {
                Log.e("!!!", "skipping : " + download.getId().toString());
                continue;
            }

            ClientCheckResult clientCheckResult = downloadCheck.isAllowedToDownload(download);
            if (clientCheckResult.isAllowed()) {
                Log.e("!!!", "downloading : " + download.getId().toString());
                download(download.getId());
                return true;
            } else {
                // todo broadcast/callback download denied
                return false;
            }
        }
        return false;
    }

    private boolean canSkipDownload(Download download) {
        return download.getStage().noActionRequired();
    }

    private boolean hasActiveDownload(List<Download> allDownloads) {
        for (Download download : allDownloads) {
            if (download.getStage().isActive()) {
                return true;
            }
        }
        return false;
    }

    private void updateTotalBytesFor(List<Download> allDownloads) {
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

    private void download(final DownloadId downloadId) {
        downloadHandler.setDownloadSubmitted(downloadId);
        executor.submit(new DownloadTask(downloadId, downloadHandler, pauser));
    }

    public void release() {
        executor.shutdown();
    }

}
