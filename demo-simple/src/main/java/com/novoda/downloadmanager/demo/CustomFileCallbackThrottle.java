package com.novoda.downloadmanager.demo;

import android.util.Log;

import com.novoda.downloadmanager.FileCallbackThrottle;
import com.novoda.downloadmanager.DownloadBatchStatus;
import com.novoda.downloadmanager.DownloadBatchStatusCallback;

// Must be public
public class CustomFileCallbackThrottle implements FileCallbackThrottle {

    private static final String TAG = CustomFileCallbackThrottle.class.getSimpleName();
    private DownloadBatchStatusCallback callback;

    @Override
    public void setCallback(DownloadBatchStatusCallback callback) {
        Log.v(TAG, "setCallback");
        this.callback = callback;
    }

    @Override
    public void update(DownloadBatchStatus downloadBatchStatus) {
        Log.v(TAG, "update " + downloadBatchStatus.getDownloadBatchTitle().asString()
                + ", progress: " + downloadBatchStatus.percentageDownloaded() + "%");

        if (callback == null) {
            return;
        }

        // no throttle is done, we call the callback immediately
        callback.onUpdate(downloadBatchStatus);
    }

    @Override
    public void stopUpdates() {
        Log.v(TAG, "stopUpdates");
    }
}
