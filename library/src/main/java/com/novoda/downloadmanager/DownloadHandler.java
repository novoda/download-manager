package com.novoda.downloadmanager;

import com.novoda.downloadmanager.domain.Download;
import com.novoda.downloadmanager.domain.DownloadFile;
import com.novoda.downloadmanager.domain.DownloadId;
import com.novoda.downloadmanager.domain.DownloadRequest;
import com.novoda.downloadmanager.domain.DownloadStage;

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

}
