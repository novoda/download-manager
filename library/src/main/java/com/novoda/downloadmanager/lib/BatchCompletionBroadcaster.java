package com.novoda.downloadmanager.lib;

import android.content.Context;
import android.content.Intent;

class BatchCompletionBroadcaster {

    static final String ACTION_BATCH_COMPLETE = "com.novoda.downloadmanager.BATCH_COMPLETE";
    static final String EXTRA_BATCH_ID = "com.novoda.downloadmanager.EXTRA.batch_id";

    private final Context context;
    private final String packageName;

    public static BatchCompletionBroadcaster newInstance(Context context) {
        return new BatchCompletionBroadcaster(context, context.getApplicationContext().getPackageName());
    }

    BatchCompletionBroadcaster(Context context, String packageName) {
        this.context = context;
        this.packageName = packageName;
    }

    public void notifyBatchCompletedFor(long batchId) {
        Intent intent = new Intent(ACTION_BATCH_COMPLETE);
        intent.setPackage(packageName);
        intent.putExtra(EXTRA_BATCH_ID, batchId);
        context.sendBroadcast(intent);
    }

}
