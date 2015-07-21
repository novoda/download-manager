package com.novoda.downloadmanager.demo.extended.pause_resume;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.demo.R;
import com.novoda.downloadmanager.demo.extended.Download;
import com.novoda.downloadmanager.demo.extended.QueryForDownloadsAsyncTask;
import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.lib.NotificationVisibility;
import com.novoda.downloadmanager.lib.Query;
import com.novoda.downloadmanager.lib.Request;
import com.novoda.notils.logger.simple.Log;

import java.util.ArrayList;
import java.util.List;

public class PauseResumeActivity extends AppCompatActivity implements QueryForDownloadsAsyncTask.Callback {
    private static final String BIG_FILE = "http://download.thinkbroadband.com/100MB.zip";
    private static final String BEARD_IMAGE = "http://i.imgur.com/9JL2QVl.jpg";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private DownloadManager downloadManager;
    private ListView listView;
    private PauseResumeAdapter pauseResumeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pause_resume);

        listView = (ListView) findViewById(R.id.main_downloads_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PauseResumeAdapter adapter = (PauseResumeAdapter) parent.getAdapter();
                Download item = adapter.getItem(position);
                long batchId = item.getBatchId();
                if (item.isPaused()) {
                    downloadManager.resumeBatch(batchId);
                } else {
                    downloadManager.pauseBatch(batchId);
                }
                queryForDownloads();
            }
        });
        downloadManager = DownloadManagerBuilder.from(this)
                .withVerboseLogging()
                .build();
        pauseResumeAdapter = new PauseResumeAdapter(new ArrayList<Download>());
        listView.setAdapter(pauseResumeAdapter);

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
        Query orderedQuery = new Query().orderByLiveness();
        QueryForDownloadsAsyncTask.newInstance(downloadManager, this).execute(orderedQuery);
    }

    private void enqueueSingleDownload() {
        Uri uri = Uri.parse(BIG_FILE);
        final Request request = new Request(uri)
                .setTitle("A Single Beard")
                .setDescription("Fine facial hair")
                .setBigPictureUrl(BEARD_IMAGE)
                .setDestinationInInternalFilesDir(Environment.DIRECTORY_MOVIES, "pause_resume_example.beard")
                .setNotificationVisibility(NotificationVisibility.ACTIVE_OR_COMPLETE)
                .applicationChecksFileIntegrity();

        long requestId = downloadManager.enqueue(request);
        Log.d("Download enqueued with request ID: " + requestId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getContentResolver().registerContentObserver(downloadManager.getDownloadsWithoutProgressUri(), true, updateSelf);
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
        pauseResumeAdapter.updateDownloads(downloads);
    }
}
