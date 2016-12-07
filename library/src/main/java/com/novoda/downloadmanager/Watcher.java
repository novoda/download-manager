package com.novoda.downloadmanager;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

class Watcher {

    private final List<DownloadChangeObserver> downloadChangeObservers = new ArrayList<>();
    private final ContentResolver contentResolver;

    public static Watcher newInstance(Context context) {
        return new Watcher(context.getContentResolver());
    }

    Watcher(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public void startListeningForDownloadUpdates(WatchType watchType, OnDownloadsChangedListener onDownloadsChangedListener) {
        DownloadChangeObserver downloadChangeObserver = new DownloadChangeObserver(onDownloadsChangedListener);
        downloadChangeObservers.add(downloadChangeObserver);
        contentResolver.registerContentObserver(watchType.toUri(), true, downloadChangeObserver.getContentObserver());
    }

    public void stopListeningForDownloadUpdates(OnDownloadsChangedListener onDownloadsChangedListener) {
        for (DownloadChangeObserver downloadChangeObserver : downloadChangeObservers) {
            if (downloadChangeObserver.onDownloadsChangedListener == onDownloadsChangedListener) {
                contentResolver.unregisterContentObserver(downloadChangeObserver.contentObserver);
                return;
            }
        }
    }

    static class DownloadChangeObserver {

        private final ContentObserver contentObserver;
        private final OnDownloadsChangedListener onDownloadsChangedListener;

        DownloadChangeObserver(final OnDownloadsChangedListener onDownloadsChangedListener) {
            this.contentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
                @Override
                public void onChange(boolean selfChange) {
                    onDownloadsChangedListener.onDownloadsChanged();
                }
            };
            this.onDownloadsChangedListener = onDownloadsChangedListener;
        }

        public ContentObserver getContentObserver() {
            return contentObserver;
        }
    }

}
