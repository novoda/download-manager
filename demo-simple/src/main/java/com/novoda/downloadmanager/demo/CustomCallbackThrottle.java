package com.novoda.downloadmanager.demo;

import android.util.Log;

import com.novoda.downloadmanager.CallbackThrottle;
import com.novoda.downloadmanager.DownloadBatchStatus;
import com.novoda.downloadmanager.DownloadBatchStatusCallback;

// Must be public
public class CustomCallbackThrottle implements CallbackThrottle {

    private static final String TAG = CustomCallbackThrottle.class.getSimpleName();
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
