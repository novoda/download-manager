package com.novoda.downloadmanager.demo.simple;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.demo.R;
import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.notifications.NotificationVisibility;
import com.novoda.downloadmanager.lib.Query;
import com.novoda.downloadmanager.lib.Request;

import java.util.List;

public class MainActivity extends AppCompatActivity implements QueryForDownloadsAsyncTask.Callback {

    private static final String BIG_FILE = "http://ipv4.download.thinkbroadband.com/200MB.zip";
    private static final String PENGUINS_IMAGE = "http://i.imgur.com/Y7pMO5Kb.jpg";

    private DownloadManager downloadManager;
    private RecyclerView recyclerView;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        emptyView = findViewById(R.id.main_no_downloads_view);
        recyclerView = (RecyclerView) findViewById(R.id.main_downloads_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        downloadManager = DownloadManagerBuilder.from(this)
                .build();

        setupDownloadingExample();
        setupQueryingExample();
    }

    private void setupDownloadingExample() {
        Uri uri = Uri.parse(BIG_FILE);
        final Request request = new Request(uri)
                .setDestinationInInternalFilesDir(Environment.DIRECTORY_MOVIES, "penguins.dat")
                .setNotificationVisibility(NotificationVisibility.ACTIVE_OR_COMPLETE)
                .setTitle("Family of Penguins")
                .setDescription("These are not the beards you're looking for")
                .setBigPictureUrl(PENGUINS_IMAGE);

        findViewById(R.id.main_download_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(@NonNull View v) {
                        downloadManager.enqueue(request);
                    }
                });
    }

    private void setupQueryingExample() {
        queryForDownloads();
        findViewById(R.id.main_refresh_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(@NonNull View v) {
                        queryForDownloads();
                    }
                }
        );
    }

    private void queryForDownloads() {
        QueryForDownloadsAsyncTask.newInstance(downloadManager, MainActivity.this).execute(new Query());
    }

    @Override
    public void onQueryResult(List<BeardDownload> beardDownloads) {
        recyclerView.setAdapter(new BeardDownloadAdapter(beardDownloads));
        emptyView.setVisibility(beardDownloads.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
