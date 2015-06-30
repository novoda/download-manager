package com.novoda.downloadmanager;

import android.content.ContentResolver;
import android.content.Context;

import com.novoda.downloadmanager.lib.BatchCompletionBroadcaster;
import com.novoda.downloadmanager.lib.DownloadManager;

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

    public DownloadManager build(ContentResolver contentResolver) {
        // TODO: is the parameter need here any more? Could use context.getContentResolver()
        if (contentResolver == null) {
            throw new IllegalStateException("You must use a ContentResolver with the DownloadManager.");
        }
        return new DownloadManager(context, contentResolver, verboseLogging, BatchCompletionBroadcaster.newInstance(context));
    }

}
