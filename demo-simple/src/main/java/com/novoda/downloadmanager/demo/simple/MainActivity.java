package com.novoda.downloadmanager.demo.simple;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.novoda.downloadmanager.Download;
import com.novoda.downloadmanager.DownloadId;
import com.novoda.downloadmanager.DownloadRequest;
import com.novoda.downloadmanager.Downloader;
import com.novoda.downloadmanager.OnDownloadsChangedListener;
import com.novoda.downloadmanager.OnDownloadsUpdateListener;
import com.novoda.downloadmanager.WatchType;
import com.novoda.downloadmanager.demo.R;
import com.novoda.notils.logger.simple.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String BIG_FILE_URL = "http://ipv4.download.thinkbroadband.com/100MB.zip";
    private static final String SMALL_FILE_URL = "http://ipv4.download.thinkbroadband.com/5MB.zip";
    private static final String PENGUINS_IMAGE_URL = "http://i.imgur.com/Y7pMO5Kb.jpg";

    private final DeleteAllDownloadsAction deleteAllDownloadsAction = new DeleteAllDownloadsAction();
    private final StartDownloadsAction startDownloadAction = new StartDownloadsAction();

    private View emptyView;
    private DownloadAdapter adapter;

    private Downloader downloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.setShowLogs(true);

        downloader = new Downloader.Builder().build(this);

        emptyView = findViewById(R.id.main_no_downloads_view);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.main_downloads_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DownloadAdapter(onDownloadItemActionsListener);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.demo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_start_downloads:
                startDownloadAction.run();
                return true;
            case R.id.menu_delete_all:
                deleteAllDownloadsAction.run();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private final DownloadAdapter.OnDownloadClickedListener onDownloadItemActionsListener = new DownloadAdapter.OnDownloadClickedListener() {

        @Override
        public void onDeleteDownload(Download download) {
            downloader.delete(download.getId());
        }

        @Override
        public void onPauseDownload(Download download) {
            toastMessage("Pausing download!");
            downloader.pause(download.getId());
        }

        @Override
        public void onResumeDownload(Download download) {
            toastMessage("Resuming download!");
            downloader.resume(download.getId());
        }

        private void toastMessage(String message) {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        downloader.addOnDownloadsUpdateListener(onDownloadsUpdate);
        downloader.startListeningForDownloadUpdates(WatchType.PROGRESS, onDownloadsChangedListener);
        downloader.requestDownloadsUpdate();
    }

    @Override
    protected void onPause() {
        downloader.removeOnDownloadsUpdateListener(onDownloadsUpdate);
        downloader.stopListeningForDownloadUpdates(onDownloadsChangedListener);
        super.onPause();
    }

    private final OnDownloadsUpdateListener onDownloadsUpdate = new OnDownloadsUpdateListener() {
        @Override
        public void onDownloadsUpdate(List<Download> downloads) {
            deleteAllDownloadsAction.update(downloads);
            adapter.update(downloads);
            emptyView.setVisibility(downloads.isEmpty() ? View.VISIBLE : View.GONE);
        }
    };

    private class StartDownloadsAction implements Runnable {

        @Override
        public void run() {
            DownloadRequest downloadRequest = createDownloadRequest();
            downloader.submit(downloadRequest);
        }

        private DownloadRequest createDownloadRequest() {
            return new DownloadRequest.Builder()
                    .with(downloader.createDownloadId())
                    .withFile(createFileRequest(SMALL_FILE_URL, "very.small_" + UUID.randomUUID().toString()))
                    .withFile(createFileRequest(BIG_FILE_URL, "very.big_" + UUID.randomUUID().toString()))
                    .withFile(createFileRequest(PENGUINS_IMAGE_URL, "very.penguin_" + UUID.randomUUID().toString()))
                    .build();
        }

        private DownloadRequest.File createFileRequest(String uri, String filename) {
            File file = new File(getCacheDir(), filename);
            Log.e("!!!", "createRequest file : " + filename);
            return new DownloadRequest.File.Builder()
                    .with(uri)
                    .withLocalUri(file.getAbsolutePath())
                    .build();
        }
    }

    private class DeleteAllDownloadsAction implements Runnable {

        private final List<DownloadId> downloadIds = new ArrayList<>();

        public void update(List<Download> downloads) {
            this.downloadIds.clear();
            for (Download download : downloads) {
                downloadIds.add(download.getId());
            }
        }

        @Override
        public void run() {
            for (DownloadId downloadId : downloadIds) {
                downloader.delete(downloadId);
            }
        }
    }

    private final OnDownloadsChangedListener onDownloadsChangedListener = new OnDownloadsChangedListener() {
        @Override
        public void onDownloadsChanged() {
            downloader.requestDownloadsUpdate();
        }
    };
}
