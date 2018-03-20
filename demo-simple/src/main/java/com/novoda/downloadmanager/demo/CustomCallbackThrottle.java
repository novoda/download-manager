package com.novoda.downloadmanager.demo;

import com.novoda.downloadmanager.CallbackThrottle;
import com.novoda.downloadmanager.DownloadBatchStatus;
import com.novoda.downloadmanager.DownloadBatchStatusCallback;
import com.novoda.notils.logger.simple.Log;

// Must be public
public class CustomCallbackThrottle implements CallbackThrottle {

    private DownloadBatchStatusCallback callback;

    @Override
    public void setCallback(DownloadBatchStatusCallback callback) {
        Log.v("setCallback");
        this.callback = callback;
    }

    @Override
    public void update(DownloadBatchStatus downloadBatchStatus) {
        Log.v("update " + downloadBatchStatus.getDownloadBatchTitle().asString()
                      + ", progress: " + downloadBatchStatus.percentageDownloaded() + "%");

        if (callback == null) {
            return;
        }

        // no throttle is done, we call the callback immediately
        callback.onUpdate(downloadBatchStatus);
    }

    @Override
    public void stopUpdates() {
        Log.v("stopUpdates");
    }
}
