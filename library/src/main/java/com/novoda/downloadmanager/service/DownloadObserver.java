package com.novoda.downloadmanager.service;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Handler;

import com.novoda.downloadmanager.Provider;

class DownloadObserver {

    private final Handler updateHandler;
    private final ContentResolver contentResolver;

    private ContentObserver contentObserver;

    DownloadObserver(Handler updateHandler, ContentResolver contentResolver) {
        this.updateHandler = updateHandler;
        this.contentResolver = contentResolver;
    }

    void startMonitoringDownloadChanges(final Callback callback) {
        contentObserver = new ContentObserver(updateHandler) {
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
