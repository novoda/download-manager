package com.novoda.downloadmanager;

import android.content.ContentResolver;
import android.database.ContentObserver;

class DownloadObserver {

    private final DownloadsHandler downloadsHandler;
    private final ContentResolver contentResolver;

    private ContentObserver contentObserver;

    public DownloadObserver(DownloadsHandler downloadsHandler, ContentResolver contentResolver) {
        this.downloadsHandler = downloadsHandler;
        this.contentResolver = contentResolver;
    }

    void startMonitoringDownloadChanges(final Callback callback) {
        contentObserver = new ContentObserver(downloadsHandler.getHandler()) {
            @Override
            public void onChange(boolean selfChange) {
                callback.onDownloadsTableUpdated();
            }
        };
        contentResolver.registerContentObserver(Provider.DOWNLOAD_SERVICE_UPDATE, true, contentObserver);
    }

    void release() {
        contentResolver.unregisterContentObserver(contentObserver);
    }

    interface Callback {
        void onDownloadsTableUpdated();
    }

}
