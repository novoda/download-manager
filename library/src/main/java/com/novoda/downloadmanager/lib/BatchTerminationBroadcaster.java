package com.novoda.downloadmanager.lib;

import android.content.Context;
import android.content.Intent;

class BatchTerminationBroadcaster {

    static final String ACTION_BATCH_COMPLETE = "com.novoda.downloadmanager.action.BATCH_COMPLETE";
    static final String ACTION_BATCH_FAILED = "com.novoda.downloadmanager.action.BATCH_FAILED";

    static final String EXTRA_BATCH_ID = DownloadReceiver.EXTRA_BATCH_ID;

    private final Context context;
    private final String packageName;

    BatchTerminationBroadcaster(Context context, String packageName) {
        this.context = context;
        this.packageName = packageName;
    }

    public void notifyBatchCompletedFor(long batchId) {
        Intent intent = new Intent(ACTION_BATCH_COMPLETE);
        intent.setPackage(packageName);
        intent.putExtra(EXTRA_BATCH_ID, batchId);
        context.sendBroadcast(intent);
    }

    public void notifyBatchFailedFor(long batchId) {
        Intent intent = new Intent(ACTION_BATCH_FAILED);
        intent.setPackage(packageName);
        intent.putExtra(EXTRA_BATCH_ID, batchId);
        context.sendBroadcast(intent);
    }

}
