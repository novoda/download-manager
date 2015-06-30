package com.novoda.downloadmanager.lib;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

class BatchCompletionBroadcaster {
    static final String ACTION_BATCH_COMPLETE = "com.novoda.downloadmanager.BATCH_COMPLETE";
    static final String EXTRA_BATCH_ID = "com.novoda.downloadmanager.EXTRA.batch_id";

    private final LocalBroadcastManager localBroadcastManager;
    private final String packageName;

    public static BatchCompletionBroadcaster newInstance(Context context) {
        return new BatchCompletionBroadcaster(LocalBroadcastManager.getInstance(context), context.getApplicationContext().getPackageName());
    }

    BatchCompletionBroadcaster(LocalBroadcastManager localBroadcastManager, String packageName) {
        this.localBroadcastManager = localBroadcastManager;
        this.packageName = packageName;
    }

    public void notifyBatchCompletedFor(long batchId) {
        Intent intent = new Intent(ACTION_BATCH_COMPLETE);
        intent.setPackage(packageName);
        intent.putExtra(EXTRA_BATCH_ID, batchId);
        localBroadcastManager.sendBroadcast(intent);
    }
}
