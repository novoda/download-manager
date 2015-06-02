package com.novoda.downloadmanager;

import android.content.ContentResolver;

import com.novoda.downloadmanager.lib.DownloadManager;

public class DownloadManagerBuilder {

    private ContentResolver contentResolver;
    private boolean verboseLogging;

    public static DownloadManagerBuilder newInstance() {
        return new DownloadManagerBuilder();
    }

    public DownloadManagerBuilder withVerboseLogging() {
        this.verboseLogging = true;
        return this;
    }

    public DownloadManagerBuilder with(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
        return this;
    }

    public DownloadManager build() {
        if (contentResolver == null) {
            throw new IllegalStateException("You must use a ContentResolver with the DownloadManager. (use with(ContentResolver resolver);");
        }
        return new DownloadManager(contentResolver, verboseLogging);
    }

}
