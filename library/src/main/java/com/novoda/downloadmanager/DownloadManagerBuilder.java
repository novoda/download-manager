package com.novoda.downloadmanager;

import android.content.ContentResolver;
import android.content.Context;

import com.novoda.downloadmanager.lib.DownloadManager;

public class DownloadManagerBuilder {

    private final Context context;

    private boolean verboseLogging;

    DownloadManagerBuilder(Context context) {
        this.context = context;
    }

    public static DownloadManagerBuilder from(Context context) {
        return new DownloadManagerBuilder(context.getApplicationContext());
    }

    public DownloadManagerBuilder withVerboseLogging() {
        this.verboseLogging = true;
        return this;
    }

    public DownloadManager build(ContentResolver contentResolver) {
        if (contentResolver == null) {
            throw new IllegalStateException("You must use a ContentResolver with the DownloadManager.");
        }
        return new DownloadManager(context, contentResolver, verboseLogging);
    }

}
