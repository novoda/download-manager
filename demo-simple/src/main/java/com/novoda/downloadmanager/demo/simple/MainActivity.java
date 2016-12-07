package com.novoda.downloadmanager.demo.simple;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.novoda.downloadmanager.Downloader;
import com.novoda.downloadmanager.OnDownloadsChangedListener;
import com.novoda.downloadmanager.OnDownloadsUpdateListener;
import com.novoda.downloadmanager.WatchType;
import com.novoda.downloadmanager.demo.R;
import com.novoda.downloadmanager.domain.Download;
import com.novoda.downloadmanager.domain.DownloadRequest;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String BIG_FILE = "http://ipv4.download.thinkbroadband.com/100MB.zip";
    private static final String SMALL_FILE = "http://ipv4.download.thinkbroadband.com/5MB.zip";
    private static final String PENGUINS_IMAGE = "http://i.imgur.com/Y7pMO5Kb.jpg";

    private Downloader downloader;
    private View emptyView;
    private DownloadAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        downloader = new Downloader.Builder().build(this);

        emptyView = findViewById(R.id.main_no_downloads_view);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.main_downloads_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DownloadAdapter(onDownloadClickedListener);
        recyclerView.setAdapter(adapter);

        setupDownloadingExample();
        setupQueryingExample();
    }

    private void setupDownloadingExample() {
        findViewById(R.id.main_download_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(@NonNull View v) {
                        DownloadRequest.File bigFile = createFileRequest(BIG_FILE, "super_big.file");
                        DownloadRequest.File bigFile2 = createFileRequest(PENGUINS_IMAGE, "penguins.important");
                        DownloadRequest.File bigFile3 = createFileRequest(SMALL_FILE, "very.small");
                        DownloadRequest downloadRequest = new DownloadRequest.Builder()
                                .with(downloader.createDownloadId())
                                .withFile(bigFile)
                                .withFile(bigFile2)
                                .withFile(bigFile3)
                                .build();
                        downloader.submit(downloadRequest);
                    }
                });
    }

    private DownloadRequest.File createFileRequest(String uri, String filename) {
        File file = new File(getCacheDir(), filename);
        return new DownloadRequest.File(uri, file.getAbsolutePath(), filename);
    }

    private void setupQueryingExample() {
        downloader.addOnDownloadsUpdateListener(onDownloadsUpdate);
        downloader.requestDownloadsUpdate();
    }

    private final OnDownloadsUpdateListener onDownloadsUpdate = new OnDownloadsUpdateListener() {
        @Override
        public void onDownloadsUpdate(List<Download> downloads) {
            adapter.update(downloads);
            emptyView.setVisibility(downloads.isEmpty() ? View.VISIBLE : View.GONE);
        }
    };

    private final DownloadAdapter.OnDownloadClickedListener onDownloadClickedListener = new DownloadAdapter.OnDownloadClickedListener() {
        @Override
        public void onDownloadClicked(Download download) {
            switch (download.getStatus()) {
                case RUNNING:
                    Toast.makeText(MainActivity.this, "Pausing download!", Toast.LENGTH_SHORT).show();
                    downloader.pause(download.getId());
                    break;

                case PAUSED:
                    Toast.makeText(MainActivity.this, "Resuming download!", Toast.LENGTH_SHORT).show();
                    downloader.resume(download.getId());
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        downloader.startListeningForDownloadUpdates(WatchType.PROGRESS, onDownloadsChangedListener);
    }

    private final OnDownloadsChangedListener onDownloadsChangedListener = new OnDownloadsChangedListener() {
        @Override
        public void onDownloadsChanged() {
            downloader.requestDownloadsUpdate();
        }
    };

    @Override
    protected void onPause() {
        downloader.removeOnDownloadsUpdateListener(onDownloadsUpdate);
        downloader.stopListeningForDownloadUpdates(onDownloadsChangedListener);
        super.onPause();
    }
}
