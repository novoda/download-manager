package com.novoda.downloadmanager.demo.extended.batches;

import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.demo.R;
import com.novoda.downloadmanager.demo.extended.Download;
import com.novoda.downloadmanager.demo.extended.QueryForDownloadsAsyncTask;
import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.lib.NotificationVisibility;
import com.novoda.downloadmanager.lib.Query;
import com.novoda.downloadmanager.lib.Request;
import com.novoda.downloadmanager.lib.RequestBatch;
import com.novoda.downloadmanager.lib.logger.LLog;

import java.util.ArrayList;
import java.util.List;

public class BatchDownloadsActivity extends AppCompatActivity implements QueryForDownloadsAsyncTask.Callback {
    private static final String BIG_FILE = "http://download.thinkbroadband.com/10MB.zip";
    private static final String BEARD_IMAGE = "http://i.imgur.com/9JL2QVl.jpg";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private DownloadManager downloadManager;
    private BatchDownloadsAdapter batchDownloadsAdapter;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batches);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.main_downloads_list);
        downloadManager = DownloadManagerBuilder.from(this)
                .build();
        batchDownloadsAdapter = new BatchDownloadsAdapter(new ArrayList<Download>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(batchDownloadsAdapter);

        emptyView = findViewById(R.id.main_no_downloads_view);

        findViewById(R.id.batch_download_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(@NonNull View v) {
                        enqueueBatch();
                    }
                });

        findViewById(R.id.batch_show_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(@NonNull View v) {
                        startActivity(new Intent(BatchDownloadsActivity.this, BatchesActivity.class));
                    }
                });

        setupQueryingExample();
    }

    private void setupQueryingExample() {
        queryForDownloads();
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
                .withExtraData("An extra beard.")
                .build();

        Uri uri = Uri.parse(BIG_FILE);
        final Request request = new Request(uri);
        request.setDestinationInInternalFilesDir(Environment.DIRECTORY_MOVIES, "beard.shipment");
        request.setNotificationExtra("beard_1");
        request.applicationChecksFileIntegrity();
        batch.addRequest(request);

        request.setNotificationExtra("beard_2");
        batch.addRequest(request);
        request.setNotificationExtra("beard_3");
        batch.addRequest(request);
        request.setNotificationExtra("beard_4");
        batch.addRequest(request);
        request.setNotificationExtra("beard_5");
        batch.addRequest(request);

        long batchId = downloadManager.enqueue(batch);
        LLog.d("Download enqueued with batch ID: " + batchId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getContentResolver().registerContentObserver(downloadManager.getContentUri(), true, updateSelf);
    }

    private final ContentObserver updateSelf = new ContentObserver(handler) {

        @Override
        public void onChange(boolean selfChange) {
            queryForDownloads();
        }

    };

    @Override
    protected void onStop() {
        super.onStop();
        getContentResolver().unregisterContentObserver(updateSelf);
    }

    @Override
    public void onQueryResult(List<Download> downloads) {
        batchDownloadsAdapter.updateDownloads(downloads);
        emptyView.setVisibility(downloads.isEmpty() ? View.VISIBLE : View.GONE);
    }

}
