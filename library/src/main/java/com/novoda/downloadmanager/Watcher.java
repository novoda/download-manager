package com.novoda.downloadmanager;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;

class Watcher {

    private final ContentResolver contentResolver;

    private OnDownloadsChangedListener onDownloadsChangedListener = OnDownloadsChangedListener.NULL_IMPL;

    public static Watcher newInstance(Context context) {
        return new Watcher(context.getContentResolver());
    }

    Watcher(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public void startListeningForDownloadUpdates(WatchType watchType, OnDownloadsChangedListener onDownloadsChangedListener) {
        this.onDownloadsChangedListener = onDownloadsChangedListener;
        contentResolver.registerContentObserver(watchType.toUri(), true, contentObserver);
    }

    public void stopListeningForDownloadUpdates() {
        contentResolver.unregisterContentObserver(contentObserver);
        onDownloadsChangedListener = OnDownloadsChangedListener.NULL_IMPL;

    }

    private final ContentObserver contentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange) {
            onDownloadsChangedListener.onDownloadsChanged();
        }
    };

    interface OnDownloadsChangedListener {
        OnDownloadsChangedListener NULL_IMPL = new OnDownloadsChangedListener() {
            @Override
            public void onDownloadsChanged() {
                // do nothing
            }
        };

        void onDownloadsChanged();

    }

}
