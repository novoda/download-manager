package com.novoda.downloadmanager.service;

import com.novoda.downloadmanager.domain.DownloadFile;

public class DownloadFileFixtures {

    private String uri = "uri";
    private int currentSize = 100;
    private int totalSize = 200;
    private String localUri = "local_uri";
    private DownloadFile.FileStatus fileStatus = DownloadFile.FileStatus.INCOMPLETE;

    public static DownloadFileFixtures aDownloadFile() {
        return new DownloadFileFixtures();
    }

    public DownloadFileFixtures withUri(String uri) {
        this.uri = uri;
        return this;
    }

    public DownloadFileFixtures withCurrentSize(int currentSize) {
        this.currentSize = currentSize;
        return this;
    }

    public DownloadFileFixtures withTotalSize(int totalSize) {
        this.totalSize = totalSize;
        return this;
    }

    public DownloadFileFixtures withLocalUri(String localUri) {
        this.localUri = localUri;
        return this;
    }

    public DownloadFileFixtures withFileStatus(DownloadFile.FileStatus fileStatus) {
        this.fileStatus = fileStatus;
        return this;
    }

    public DownloadFile build() {
        return new DownloadFile(uri, currentSize, totalSize, localUri, fileStatus);
    }

}
