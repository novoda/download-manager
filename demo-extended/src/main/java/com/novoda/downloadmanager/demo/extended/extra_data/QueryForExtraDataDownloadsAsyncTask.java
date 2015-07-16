package com.novoda.downloadmanager.demo.extended.extra_data;

import android.database.Cursor;
import android.os.AsyncTask;

import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.lib.Query;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class QueryForExtraDataDownloadsAsyncTask extends AsyncTask<Query, Void, List<ExtraDataDownload>> {

    private final DownloadManager downloadManager;
    private final WeakReference<Callback> weakCallback;

    public static QueryForExtraDataDownloadsAsyncTask newInstance(DownloadManager downloadManager, Callback callback) {
        return new QueryForExtraDataDownloadsAsyncTask(downloadManager, new WeakReference<>(callback));
    }

    QueryForExtraDataDownloadsAsyncTask(DownloadManager downloadManager, WeakReference<Callback> weakCallback) {
        this.downloadManager = downloadManager;
        this.weakCallback = weakCallback;
    }

    @Override
    protected List<ExtraDataDownload> doInBackground(Query... params) {
        Cursor cursor = downloadManager.query(params[0]);
        List<ExtraDataDownload> extraDataDownloads = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE));
                String fileName = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_EXTRA_DATA));
                extraDataDownloads.add(new ExtraDataDownload(title, fileName));
            }
        } finally {
            cursor.close();
        }
        return extraDataDownloads;
    }

    @Override
    protected void onPostExecute(List<ExtraDataDownload> extraDataDownloads) {
        super.onPostExecute(extraDataDownloads);
        Callback callback = weakCallback.get();
        if (callback == null) {
            return;
        }
        callback.onQueryResult(extraDataDownloads);
    }

    public interface Callback {
        void onQueryResult(List<ExtraDataDownload> extraDataDownloads);
    }
}
