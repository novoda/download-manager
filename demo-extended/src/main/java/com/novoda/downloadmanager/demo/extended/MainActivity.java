package com.novoda.downloadmanager.demo.extended;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.demo.R;
import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.lib.NotificationVisibility;
import com.novoda.downloadmanager.lib.Query;
import com.novoda.downloadmanager.lib.Request;
import com.novoda.downloadmanager.lib.RequestBatch;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements QueryForDownloadsAsyncTask.Callback {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String BIG_FILE = "http://download.thinkbroadband.com/20MB.zip";
    private static final String BEARD_IMAGE = "http://i.imgur.com/9JL2QVl.jpg";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private DownloadManager downloadManager;
    private ListView listView;
    private DownloadAdapter downloadAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.novoda.notils.logger.simple.Log.setShowLogs(true);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.main_downloads_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DownloadAdapter adapter = (DownloadAdapter) parent.getAdapter();
                Download item = adapter.getItem(position);
                long batchId = item.getBatchId();
                if (item.isPaused()) {
                    downloadManager.resumeBatch(batchId);
                } else {
                    downloadManager.pauseBatch(batchId);
                }
            }
        });
        downloadManager = DownloadManagerBuilder.from(this)
                .withVerboseLogging()
                .build();
        downloadAdapter = new DownloadAdapter(new ArrayList<Download>(), new DownloadAdapter.Listener() {
            @Override
            public void onDelete(Download download) {
                downloadManager.removeBatches(download.getBatchId());
            }
        });
        listView.setAdapter(downloadAdapter);

        findViewById(R.id.single_download_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(@NonNull View v) {
                        enqueueSingleDownload();
                    }
                });

        findViewById(R.id.batch_download_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(@NonNull View v) {
                        enqueueBatch();
                    }
                });

        setupQueryingExample();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getContentResolver().registerContentObserver(DownloadManager.CONTENT_URI, true, updateSelf);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getContentResolver().unregisterContentObserver(updateSelf);
    }

    private void enqueueSingleDownload() {
        Uri uri = Uri.parse(BIG_FILE);
        final Request request = new Request(uri)
                .setTitle("A Single Beard")
                .setDescription("Fine facial hair")
                .setBigPictureUrl(BEARD_IMAGE)
                .setDestinationInInternalFilesDir(Environment.DIRECTORY_MOVIES, "example.beard")
                .setNotificationVisibility(NotificationVisibility.ACTIVE_OR_COMPLETE);

        long requestId = downloadManager.enqueue(request);
        Log.d(TAG, "Download enqueued with request ID: " + requestId);
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
        Log.d(TAG, "Download enqueued with batch ID: " + batchId);
    }

    private void setupQueryingExample() {
        queryForDownloads();
        listView.setEmptyView(findViewById(R.id.main_no_downloads_view));
    }

    private void queryForDownloads() {
        QueryForDownloadsAsyncTask.newInstance(downloadManager, this).execute(new Query());
    }

    @Override
    public void onQueryResult(List<Download> downloads) {
        downloadAdapter.updateDownloads(downloads);
    }

    private final ContentObserver updateSelf = new ContentObserver(handler) {

        @Override
        public void onChange(boolean selfChange) {
            queryForDownloads();
        }

    };

}
