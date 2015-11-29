package com.novoda.downloadmanager;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.novoda.downloadmanager.domain.Download;
import com.novoda.downloadmanager.domain.DownloadId;
import com.novoda.downloadmanager.domain.DownloadRequest;
import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.List;

public class Downloader {

    private final DownloadHandler downloadHandler;
    private final Context context;
    private final Pauser pauser;
    private final Listeners listeners;
    private final Watcher watcher;

    public static Downloader from(Context context) {
        Context applicationContext = context.getApplicationContext();
        ContentResolver contentResolver = applicationContext.getContentResolver();
        DatabaseInteraction databaseInteraction = new DatabaseInteraction(contentResolver);
        ContentLengthFetcher contentLengthFetcher = new ContentLengthFetcher(new OkHttpClient());
        DownloadHandler downloadHandler = new DownloadHandler(databaseInteraction, contentLengthFetcher);
        Pauser pauser = new Pauser(LocalBroadcastManager.getInstance(context));
        Listeners listeners = Listeners.newInstance();
        Watcher watcher = Watcher.newInstance(context);
        return new Downloader(downloadHandler, applicationContext, pauser, listeners, watcher);
    }

    public Downloader(DownloadHandler downloadHandler, Context context, Pauser pauser, Listeners listeners, Watcher watcher) {
        this.downloadHandler = downloadHandler;
        this.context = context;
        this.pauser = pauser;
        this.listeners = listeners;
        this.watcher = watcher;
    }

    public DownloadId createDownloadId() {
        return downloadHandler.createDownloadId();
    }

    public void submit(DownloadRequest downloadRequest) {
        downloadHandler.submitRequest(downloadRequest);
        context.startService(new Intent(context, Service.class));
    }

    public void pause(DownloadId downloadId) {
        pauser.requestPause(downloadId);
    }

    public void resume(DownloadId downloadId) {
        Log.e("!!!", "asked to resume");
        downloadHandler.resumeDownload(downloadId);
        context.startService(new Intent(context, Service.class));
    }

    public List<Download> getAllDownloads() {
        return downloadHandler.getAllDownloads();
    }

    public void addOnDownloadsUpdateListener(OnDownloadsUpdateListener listener) {
        listeners.addOnDownloadsUpdateListener(listener);
    }

    public void removeOnDownloadsUpdateListener(OnDownloadsUpdateListener listener) {
        listeners.removeOnDownloadsUpdateListener(listener);
    }

    public void requestDownloadsUpdate() {
        List<Download> allDownloads = getAllDownloads();
        listeners.notify(allDownloads);
    }

    public void startListeningForDownloadUpdates(WatchType watchType) {
        watcher.startListeningForDownloadUpdates(watchType, onDownloadsChangedListener);
    }

    private final Watcher.OnDownloadsChangedListener onDownloadsChangedListener = new Watcher.OnDownloadsChangedListener() {
        @Override
        public void onDownloadsChanged() {
            requestDownloadsUpdate();
        }
    };

    public void stopListeningForDownloadUpdates() {
        watcher.stopListeningForDownloadUpdates();
    }

    public interface OnDownloadsUpdateListener {

        void onDownloadsUpdate(List<Download> downloads);

    }

    private static class Watcher {

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

    private static class Requester {

        public void requestDownloadsUpdate() {

        }

    }

    static class Listeners {

        private final List<OnDownloadsUpdateListener> listeners;

        public static Listeners newInstance() {
            return new Listeners(new ArrayList<OnDownloadsUpdateListener>());
        }

        Listeners(List<OnDownloadsUpdateListener> listeners) {
            this.listeners = listeners;
        }

        public void addOnDownloadsUpdateListener(OnDownloadsUpdateListener listener) {
            listeners.add(listener);
        }

        public void removeOnDownloadsUpdateListener(OnDownloadsUpdateListener listener) {
            listeners.remove(listener);
        }

        public void notify(List<Download> downloads) {
            for (OnDownloadsUpdateListener listener : listeners) {
                listener.onDownloadsUpdate(downloads);
            }

        }

    }

    public enum WatchType {
        STATUS_CHANGE {
            @Override
            Uri toUri() {
                return Provider.DOWNLOAD_STATUS_UPDATE;
            }
        },
        PROGRESS {
            @Override
            Uri toUri() {
                return Provider.DOWNLOAD_PROGRESS_UPDATE;
            }
        };

        abstract Uri toUri();
    }

}
