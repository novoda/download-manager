package com.novoda.downloadmanager;

import android.content.ContentResolver;

import com.novoda.downloadmanager.lib.DownloadManager;

public class DownloadManagerBuilder {

    private boolean verboseLogging;

    DownloadManagerBuilder() {
        // use the create method, Alex's favourite
    }

    public static DownloadManagerBuilder create() {
        return new DownloadManagerBuilder();
    }

    public DownloadManagerBuilder withVerboseLogging() {
        this.verboseLogging = true;
        return this;
    }

    public DownloadManager build(ContentResolver contentResolver) {
        if (contentResolver == null) {
            throw new IllegalStateException("You must use a ContentResolver with the DownloadManager.");
        }
        return new DownloadManager(contentResolver, verboseLogging);
    }

}
