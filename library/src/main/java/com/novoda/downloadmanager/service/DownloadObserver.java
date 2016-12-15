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

    void startMonitoringDownloadChanges(final OnUpdate onUpdate) {
        contentObserver = new ContentObserver(updateHandler) {
            @Override
            public void onChange(boolean selfChange) {
                onUpdate.onUpdate();
            }
        };
        contentResolver.registerContentObserver(Provider.DOWNLOAD_SERVICE_UPDATE, true, contentObserver);
    }

    void release() {
        contentResolver.unregisterContentObserver(contentObserver);
    }

    interface OnUpdate {
        void onUpdate();
    }

}
