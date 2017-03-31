package com.novoda.downloadmanager.service;

import com.novoda.downloadmanager.domain.DownloadId;

import java.util.ArrayList;
import java.util.List;

public class SubmittedDownloadsTracker {

    private final List<DownloadId> submittedDownloads = new ArrayList<>();

    public void addDownloadId(DownloadId downloadId) {
        submittedDownloads.add(downloadId);
    }

    public boolean contains(DownloadId downloadId) {
        return submittedDownloads.contains(downloadId);
    }

}
