package com.novoda.downloadmanager.demo.extended;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.novoda.downloadmanager.lib.DownloadManager;

public class DownloadBatchCompletionReceiver extends BroadcastReceiver {

    private static final int UNKNOWN_BATCH_ID = -1;
    private static final String TAG = DownloadBatchCompletionReceiver.class.getSimpleName();

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        long batchId = intent.getLongExtra(DownloadManager.EXTRA_BATCH_ID, UNKNOWN_BATCH_ID);
        Toast.makeText(context, "Batch completed with id: " + batchId, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Batch completed: " + batchId);
    }

}
