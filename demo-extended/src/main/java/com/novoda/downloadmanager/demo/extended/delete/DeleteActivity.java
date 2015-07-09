package com.novoda.downloadmanager.demo.extended.delete;

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
import com.novoda.downloadmanager.lib.NotificationVisibility;
import com.novoda.downloadmanager.lib.Query;
import com.novoda.downloadmanager.lib.Request;
import com.novoda.notils.logger.simple.Log;

import java.util.ArrayList;
import java.util.List;

public class DeleteActivity extends AppCompatActivity implements QueryForDownloadsAsyncTask.Callback {
    private static final String BIG_FILE = "http://download.thinkbroadband.com/100MB.zip";
    private static final String BEARD_IMAGE = "http://i.imgur.com/9JL2QVl.jpg";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private DownloadManager downloadManager;
    private ListView listView;
    private DownloadAdapter downloadAdapter;

    private final QueryTimestamp lastQueryTimestamp = new QueryTimestamp();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);
        listView = (ListView) findViewById(R.id.main_downloads_list);
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

        setupQueryingExample();
    }

    private void setupQueryingExample() {
        queryForDownloads();
        listView.setEmptyView(findViewById(R.id.main_no_downloads_view));
    }

    private void queryForDownloads() {
        QueryForDownloadsAsyncTask.newInstance(downloadManager, this).execute(new Query());
    }

    private void enqueueSingleDownload() {
        Uri uri = Uri.parse(BIG_FILE);
        final Request request = new Request(uri)
                .setTitle("A Single Beard")
                .setDescription("Fine facial hair")
                .setBigPictureUrl(BEARD_IMAGE)
                .setDestinationInInternalFilesDir(Environment.DIRECTORY_MOVIES, "can_deleted_example.beard")
                .setNotificationVisibility(NotificationVisibility.ACTIVE_OR_COMPLETE);

        long requestId = downloadManager.enqueue(request);
        Log.d("Download enqueued with request ID: " + requestId);
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
        downloadAdapter.updateDownloads(downloads);
    }

}
