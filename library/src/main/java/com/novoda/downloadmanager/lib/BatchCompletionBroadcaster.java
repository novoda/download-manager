package com.novoda.downloadmanager.lib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

public class BatchCompletionBroadcaster {

    static final String ACTION_BATCH_COMPLETE = "com.novoda.downloadmanager.BATCH_COMPLETE";
    static final String EXTRA_BATCH_ID = "com.novoda.downloadmanager.EXTRA.batch_id";

    private final LocalBroadcastManager localBroadcastManager;
    private final String packageName;
    private BatchCompletionListener batchCompletionListener;

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

    public void registerReceiver(BatchCompletionListener batchCompletionListener) {
        this.batchCompletionListener = batchCompletionListener;
        IntentFilter batchIntentFilter = new IntentFilter(DownloadManager.ACTION_BATCH_COMPLETE);
        localBroadcastManager.registerReceiver(batchCompletedReceiver, batchIntentFilter);
    }

    public void unregisterReceiver() {
        this.batchCompletionListener = null;
        localBroadcastManager.unregisterReceiver(batchCompletedReceiver);
    }

    private final BroadcastReceiver batchCompletedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(@NonNull Context context, @NonNull Intent intent) {
            long batchId = intent.getLongExtra(DownloadManager.EXTRA_BATCH_ID, -1);

            if (batchCompletionListener != null) {
                batchCompletionListener.onBatchComplete(batchId);
            }
        }
    };

}
