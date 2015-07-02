package com.novoda.downloadmanager.demo.extended;

import java.util.ArrayList;
import java.util.List;

public class DownloadBatch {

    private final long id;
    private final List<Download> downloads;

    public DownloadBatch(long id) {
        this.id = id;
        this.downloads = new ArrayList<>();
    }

    public void add(Download download) {
        downloads.add(download);
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return downloads.get(0).getTitle();
    }

    public String getFileName() {
        return downloads.get(0).getFileName();
    }

    public String getDownloadStatusText() {
        if (isPaused()) {
            return "Paused";
        }
        if (isCompleted()) {
            return "Completed";
        }
        if (isDownloading()) {
            return "Downloading";
        }
        return "Pending";
    }

    private boolean isDownloading() {
        for (Download download : downloads) {
            if (download.isDownloading()) {
                return true;
            }
        }
        return false;
    }

    public boolean isPaused() {
        for (Download download : downloads) {
            if (download.isPaused()) {
                return true;
            }
        }
        return false;
    }

    private boolean isCompleted() {
        for (Download download : downloads) {
            if (!download.isCompleted()) {
                return false;
            }
        }
        return true;
    }


}
