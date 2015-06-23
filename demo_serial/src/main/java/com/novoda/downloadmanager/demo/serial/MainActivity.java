package com.novoda.downloadmanager.demo.serial;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.demo.R;
import com.novoda.downloadmanager.lib.DownloadBatch;
import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.lib.Query;
import com.novoda.downloadmanager.lib.Request;

import java.util.List;

public class MainActivity extends AppCompatActivity implements QueryForDownloadsAsyncTask.Callback {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String BIG_FILE = "http://download.thinkbroadband.com/100MB.zip";
    private static final String BEARD_IMAGE = "http://i.imgur.com/9JL2QVl.jpg";

    private DownloadManager downloadManager;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.novoda.notils.logger.simple.Log.setShowLogs(true);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.main_downloads_list);
        downloadManager = DownloadManagerBuilder.from(this)
                .withVerboseLogging()
                .build(getContentResolver());

        setupDownloadingExample();
        setupQueryingExample();
    }

    private void setupDownloadingExample() {
        Uri uri = Uri.parse(BIG_FILE);
        final DownloadBatch batch = new DownloadBatch("Large Beard Shipment", "Goatees galore", BEARD_IMAGE);

        final Request request = new Request(uri);
        request.setDestinationInInternalFilesDir(Environment.DIRECTORY_MOVIES, "beard.shipment");
        request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        findViewById(R.id.main_download_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        long batchId = downloadManager.create(batch);
                        request.setBatchId(batchId);
                        downloadManager.enqueue(request);
                        downloadManager.enqueue(request);
                        Log.d(TAG, "Download starting with batch id: " + batch);
                    }
                });
    }

    private void setupQueryingExample() {
        QueryForDownloadsAsyncTask.newInstance(downloadManager, this).execute(new Query());
        findViewById(R.id.main_refresh_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QueryForDownloadsAsyncTask.newInstance(downloadManager, MainActivity.this).execute(new Query());
                    }
                });
        listView.setEmptyView(findViewById(R.id.main_no_downloads_view));
    }

    @Override
    public void onQueryResult(List<Download> downloads) {
        listView.setAdapter(new DownloadAdapter(downloads));
    }
}
