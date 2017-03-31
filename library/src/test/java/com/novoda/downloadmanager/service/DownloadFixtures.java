package com.novoda.downloadmanager.service;

import com.novoda.downloadmanager.domain.Download;
import com.novoda.downloadmanager.domain.DownloadFile;
import com.novoda.downloadmanager.domain.DownloadId;
import com.novoda.downloadmanager.domain.DownloadStage;
import com.novoda.downloadmanager.domain.DownloadStatus;
import com.novoda.downloadmanager.domain.ExternalId;

import java.util.Collections;
import java.util.List;

class DownloadFixtures {

    private DownloadId downloadId = new DownloadId(123456);
    private int currentSize = 100;
    private int totalSize = 200;
    private DownloadStage downloadStage = DownloadStage.RUNNING;
    private DownloadStatus downloadStatus = DownloadStatus.TRANSITIONING;
    private List<DownloadFile> downloadFiles = Collections.emptyList();
    private ExternalId externalId = new ExternalId("external_id");

    public static DownloadFixtures aDownload() {
        return new DownloadFixtures();
    }

    public DownloadFixtures withDownloadId(DownloadId downloadId) {
        this.downloadId = downloadId;
        return this;
    }

    public DownloadFixtures withCurrentSize(int currentSize) {
        this.currentSize = currentSize;
        return this;
    }

    public DownloadFixtures withTotalSize(int totalSize) {
        this.totalSize = totalSize;
        return this;
    }

    public DownloadFixtures withDownloadStage(DownloadStage downloadStage) {
        this.downloadStage = downloadStage;
        return this;
    }

    public DownloadFixtures withDownloadStatus(DownloadStatus downloadStatus) {
        this.downloadStatus = downloadStatus;
        return this;
    }

    public DownloadFixtures withDownloadFiles(List<DownloadFile> downloadFiles) {
        this.downloadFiles = downloadFiles;
        return this;
    }

    public DownloadFixtures withExternalId(ExternalId externalId) {
        this.externalId = externalId;
        return this;
    }

    public Download build() {
        return new Download(downloadId, currentSize, totalSize, downloadStage, downloadStatus, downloadFiles, externalId);
    }

}
