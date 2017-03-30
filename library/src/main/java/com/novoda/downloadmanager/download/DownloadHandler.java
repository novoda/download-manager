package com.novoda.downloadmanager.download;

import com.novoda.downloadmanager.domain.Download;
import com.novoda.downloadmanager.domain.DownloadFile;
import com.novoda.downloadmanager.domain.DownloadId;
import com.novoda.downloadmanager.domain.DownloadRequest;
import com.novoda.downloadmanager.domain.DownloadStage;

import java.io.File;
import java.util.List;

public class DownloadHandler {

    private final DatabaseInteraction databaseInteraction;
    private final ContentLengthFetcher contentLengthFetcher;

    public DownloadHandler(DatabaseInteraction databaseInteraction, ContentLengthFetcher contentLengthFetcher) {
        this.databaseInteraction = databaseInteraction;
        this.contentLengthFetcher = contentLengthFetcher;
    }

    public DownloadId createDownloadId() {
        return databaseInteraction.newDownloadRequest();
    }

    public void submitRequest(DownloadRequest downloadRequest) {
        databaseInteraction.submitRequest(downloadRequest);
    }

    public void addCompletedRequest(DownloadRequest downloadRequest) {
        databaseInteraction.addCompletedRequest(downloadRequest);
    }

    public List<Download> getAllDownloads() {
        return databaseInteraction.getAllDownloads();
    }

    // TODO: maybe we can pass the download's total size (this method only called from outside, we already pass values like currentSize in other methods),
    // then we don't need contentLengthFetcher in here
    public void updateFileSize(DownloadFile file) {
        long totalBytes = contentLengthFetcher.fetchContentLengthFor(file);
        databaseInteraction.updateFileSize(file, totalBytes);
    }

    public void setDownloadSubmitted(DownloadId downloadId) {
        databaseInteraction.updateStatus(downloadId, DownloadStage.SUBMITTED);
    }

    public void updateFileProgress(DownloadFile file, long bytesWritten) {
        databaseInteraction.updateFileProgress(file, bytesWritten);
    }

    public void updateFile(DownloadFile file, DownloadFile.FileStatus status, long currentSize) {
        databaseInteraction.updateFile(file, status, currentSize);
    }

    public void setDownloadRunning(DownloadId downloadId) {
        databaseInteraction.updateStatus(downloadId, DownloadStage.RUNNING);
    }

    public void syncDownloadStatus(DownloadId downloadId) {
        Download download = databaseInteraction.getDownload(downloadId);
        if (download.getCurrentSize() == download.getTotalSize()) {
            databaseInteraction.updateStatus(downloadId, DownloadStage.COMPLETED);
        }
    }

    public Download getDownload(DownloadId downloadId) {
        return databaseInteraction.getDownload(downloadId);
    }

    public void setDownloadFailed(DownloadId downloadId) {
        databaseInteraction.updateStatus(downloadId, DownloadStage.FAILED);
    }

    public void setDownloadPaused(DownloadId downloadId) {
        databaseInteraction.updateStatus(downloadId, DownloadStage.PAUSED);
    }

    public void resumeDownload(DownloadId downloadId) {
        databaseInteraction.updateStatus(downloadId, DownloadStage.QUEUED);
    }

    public void markForDeletion(DownloadId downloadId) {
        databaseInteraction.updateStatus(downloadId, DownloadStage.MARKED_FOR_DELETION);
    }

    public void deleteMarkedBatchesFor(List<Download> allDownloads) {
        for (Download download : allDownloads) {
            if (download.getStage() == DownloadStage.MARKED_FOR_DELETION) {
                deleteFilesFor(download);
            }
        }

    }

    private void deleteFilesFor(Download download) {
        for (DownloadFile downloadFile : download.getFiles()) {
            new File(downloadFile.getLocalUri()).delete();
        }
        databaseInteraction.delete(download);
    }

}
