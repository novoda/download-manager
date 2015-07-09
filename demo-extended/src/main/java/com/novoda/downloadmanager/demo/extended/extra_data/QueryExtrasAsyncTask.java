package com.novoda.downloadmanager.demo.extended.extra_data;

import android.database.Cursor;
import android.os.AsyncTask;

import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.lib.Query;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class QueryExtrasAsyncTask extends AsyncTask<Query, Void, List<Download>> {

    private final DownloadManager downloadManager;
    private final WeakReference<Callback> weakCallback;

    public static QueryExtrasAsyncTask newInstance(DownloadManager downloadManager, Callback callback) {
        return new QueryExtrasAsyncTask(downloadManager, new WeakReference<>(callback));
    }

    QueryExtrasAsyncTask(DownloadManager downloadManager, WeakReference<Callback> weakCallback) {
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
                String fileName = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_EXTRA_DATA));
                downloads.add(new Download(title, fileName));
            }
        } finally {
            cursor.close();
        }
        return downloads;
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

    public interface Callback {
        void onQueryResult(List<Download> downloads);
    }
}
