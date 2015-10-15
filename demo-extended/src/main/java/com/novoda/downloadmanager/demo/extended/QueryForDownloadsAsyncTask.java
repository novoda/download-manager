package com.novoda.downloadmanager.demo.extended;

import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.lib.Query;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class QueryForDownloadsAsyncTask extends AsyncTask<Query, Void, List<BeardDownload>> {

    private final DownloadManager downloadManager;
    private final WeakReference<Callback> weakCallback;

    public static QueryForDownloadsAsyncTask newInstance(DownloadManager downloadManager, Callback callback) {
        return new QueryForDownloadsAsyncTask(downloadManager, new WeakReference<>(callback));
    }

    QueryForDownloadsAsyncTask(DownloadManager downloadManager, WeakReference<Callback> weakCallback) {
        this.downloadManager = downloadManager;
        this.weakCallback = weakCallback;
    }

    @Override
    protected List<BeardDownload> doInBackground(@NonNull Query... params) {
        Cursor cursor = downloadManager.query(params[0]);
        List<BeardDownload> beardDownloads = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE));
                String fileName = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_FILENAME));
                int downloadStatus = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                long batchId = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BATCH_ID));
                beardDownloads.add(new BeardDownload(title, fileName, downloadStatus, batchId));
            }
        } finally {
            cursor.close();
        }
        return beardDownloads;
    }

    @Override
    protected void onPostExecute(@NonNull List<BeardDownload> beardDownloads) {
        super.onPostExecute(beardDownloads);
        Callback callback = weakCallback.get();
        if (callback == null) {
            return;
        }
        callback.onQueryResult(beardDownloads);
    }

    public interface Callback {
        void onQueryResult(List<BeardDownload> beardDownloads);
    }
}
