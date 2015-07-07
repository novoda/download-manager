package com.novoda.downloadmanager.demo.extended.batches;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.demo.R;
import com.novoda.downloadmanager.demo.extended.Download;
import com.novoda.downloadmanager.demo.extended.DownloadAdapter;
import com.novoda.downloadmanager.demo.extended.QueryForDownloadsAsyncTask;
import com.novoda.downloadmanager.demo.extended.QueryTimestamp;
import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.lib.DownloadProvider;
import com.novoda.downloadmanager.lib.Downloads;
import com.novoda.downloadmanager.lib.NotificationVisibility;
import com.novoda.downloadmanager.lib.Query;
import com.novoda.downloadmanager.lib.Request;
import com.novoda.downloadmanager.lib.RequestBatch;
import com.novoda.notils.logger.simple.Log;

import java.util.ArrayList;
import java.util.List;

public class BatchesActivity extends AppCompatActivity implements QueryForDownloadsAsyncTask.Callback {
    private static final String BIG_FILE = "http://download.thinkbroadband.com/100MB.zip";
    private static final String BEARD_IMAGE = "http://i.imgur.com/9JL2QVl.jpg";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private DownloadManager downloadManager;
    private ListView listView;
    private DownloadAdapter downloadAdapter;
    private Downloads downloads;

    private final QueryTimestamp lastQueryTimestamp = new QueryTimestamp();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batches);
        downloads = new Downloads(DownloadProvider.AUTHORITY);
        listView = (ListView) findViewById(R.id.main_downloads_list);
        downloadManager = DownloadManagerBuilder.from(this)
                .withVerboseLogging()
                .build();
        downloadAdapter = new DownloadAdapter(new ArrayList<Download>());
        listView.setAdapter(downloadAdapter);

        findViewById(R.id.batch_download_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(@NonNull View v) {
                        enqueueBatch();
                    }
                });

        setupQueryingExample();
    }

    private void setupQueryingExample() {
        queryForDownloads();
        listView.setEmptyView(findViewById(R.id.main_no_downloads_view));
    }

    private void queryForDownloads() {
        QueryForDownloadsAsyncTask.newInstance(downloadManager, this).execute(new Query());
    }

    private void enqueueBatch() {
        final RequestBatch batch = new RequestBatch.Builder()
                .withTitle("Large Beard Shipment")
                .withDescription("Goatees galore")
                .withBigPictureUrl(BEARD_IMAGE)
                .withVisibility(NotificationVisibility.ACTIVE_OR_COMPLETE)
                .build();

        Uri uri = Uri.parse(BIG_FILE);
        final Request request = new Request(uri);
        request.setDestinationInInternalFilesDir(Environment.DIRECTORY_MOVIES, "beard.shipment");
        request.setExtra("beard_1");

        batch.addRequest(request);
        request.setExtra("beard_2");
        batch.addRequest(request);
        long batchId = downloadManager.enqueue(batch);
        Log.d("Download enqueued with batch ID: " + batchId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getContentResolver().registerContentObserver(downloads.getContentUri(), true, updateSelf);
    }

    private final ContentObserver updateSelf = new ContentObserver(handler) {

        @Override
        public void onChange(boolean selfChange) {
            if (lastQueryTimestamp.updatedRecently()) {
                return;
            }
            queryForDownloads();
            lastQueryTimestamp.setJustUpdated();
        }

    };

    @Override
    protected void onStop() {
        super.onStop();
        getContentResolver().unregisterContentObserver(updateSelf);
    }

    @Override
    public void onQueryResult(List<Download> downloads) {
        downloadAdapter.updateDownloads(downloads);
    }

}
