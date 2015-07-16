package com.novoda.downloadmanager.demo.extended.batches;

import android.database.Cursor;
import android.os.AsyncTask;

import com.novoda.downloadmanager.lib.BatchQuery;
import com.novoda.downloadmanager.lib.DownloadManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class QueryForBatchesAsyncTask extends AsyncTask<BatchQuery, Void, List<Batch>> {

    private final DownloadManager downloadManager;
    private final WeakReference<Callback> weakCallback;

    public static QueryForBatchesAsyncTask newInstance(DownloadManager downloadManager, Callback callback) {
        return new QueryForBatchesAsyncTask(downloadManager, new WeakReference<>(callback));
    }

    QueryForBatchesAsyncTask(DownloadManager downloadManager, WeakReference<Callback> weakCallback) {
        this.downloadManager = downloadManager;
        this.weakCallback = weakCallback;
    }

    @Override
    protected List<Batch> doInBackground(BatchQuery... params) {
        Cursor cursor = downloadManager.query(params[0]);
        List<Batch> batches = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE));
                int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BATCH_STATUS));
                long totalBytes = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BATCH_TOTAL_SIZE_BYTES));
                long currentBytes = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BATCH_CURRENT_SIZE_BYTES));
                String extraData = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_EXTRA_DATA));
                batches.add(new Batch(id, title, status, totalBytes, currentBytes, extraData));
            }
        } finally {
            cursor.close();
        }
        return batches;
    }

    @Override
    protected void onPostExecute(List<Batch> batches) {
        super.onPostExecute(batches);
        Callback callback = weakCallback.get();
        if (callback == null) {
            return;
        }
        callback.onQueryResult(batches);
    }

    public interface Callback {
        void onQueryResult(List<Batch> batches);
    }
}
