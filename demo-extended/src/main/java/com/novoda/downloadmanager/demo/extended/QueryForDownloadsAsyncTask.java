package com.novoda.downloadmanager.demo.extended;

import android.database.Cursor;
import android.os.AsyncTask;

import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.lib.Query;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class QueryForDownloadsAsyncTask extends AsyncTask<Query, Void, List<Download>> {

    private final DownloadManager downloadManager;
    private final WeakReference<Callback> weakCallback;

    static QueryForDownloadsAsyncTask newInstance(DownloadManager downloadManager, Callback callback) {
        return new QueryForDownloadsAsyncTask(downloadManager, new WeakReference<>(callback));
    }

    QueryForDownloadsAsyncTask(DownloadManager downloadManager, WeakReference<Callback> weakCallback) {
        this.downloadManager = downloadManager;
        this.weakCallback = weakCallback;
    }

    @Override
    protected List<Download> doInBackground(Query... params) {
        Cursor cursor = downloadManager.query(params[0]);
        List<Download> downloads = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE));
                String fileName = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_FILENAME));
                int downloadStatus = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                long id = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BATCH_ID));
                downloads.add(new Download(title, fileName, downloadStatus, id));
            }
        } finally {
            cursor.close();
        }
        sortByBatchId(downloads);
        return downloads;
    }

    private void sortByBatchId(List<Download> downloads) {
        Collections.sort(downloads, new Comparator<Download>() {
            @Override
            public int compare(Download lhs, Download rhs) {
                return (int) (lhs.getBatchId() - rhs.getBatchId());
            }
        });
    }

    @Override
    protected void onPostExecute(List<Download> downloads) {
        super.onPostExecute(downloads);
        Callback callback = weakCallback.get();
        if (callback == null) {
            return;
        }
        callback.onQueryResult(downloads);
    }

    interface Callback {
        void onQueryResult(List<Download> downloads);
    }
}
