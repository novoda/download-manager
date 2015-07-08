package com.novoda.downloadmanager;

import android.content.ContentResolver;
import android.content.Context;

import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.lib.DownloadProvider;
import com.novoda.downloadmanager.lib.Downloads;
import com.novoda.downloadmanager.lib.DownloadsUriProvider;

/**
 * A client can specify whether the downloads are allowed to proceed by implementing
 * {@link com.novoda.downloadmanager.lib.DownloadClientReadyChecker} on your Application class
 *
 */
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

    public DownloadManager build() {
        ContentResolver contentResolver = context.getContentResolver();
        DownloadsUriProvider downloadsUriProvider = DownloadsUriProvider.newInstance();
        return new DownloadManager(context, contentResolver, verboseLogging, downloadsUriProvider);
    }

}
