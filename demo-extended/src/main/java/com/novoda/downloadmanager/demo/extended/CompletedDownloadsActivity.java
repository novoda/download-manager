package com.novoda.downloadmanager.demo.extended;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.demo.R;
import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.lib.Request;
import com.novoda.downloadmanager.lib.RequestBatch;

public class CompletedDownloadsActivity extends AppCompatActivity {

    private static final Uri REQUEST_URI = Uri.parse("https://raw.githubusercontent.com/novoda/download-manager/master/RELEASE-NOTES.md");
    private DownloadManager downloadManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_downloads);
        downloadManager = DownloadManagerBuilder.from(CompletedDownloadsActivity.this)
                .build();

        findViewById(R.id.add_completed_batch).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RequestBatch requestBatch = new RequestBatch.Builder()
                                .withTitle("Completed download")
                                .withDescription("This download has already been downloaded, but will appear in the download manager API")
                                .build();
                        Request request = new Request(REQUEST_URI)
                                .setTitle("Download Manager release notes")
                                .setDescription("This file has already been downloaded")
                                .setMimeType("text/plain")
                                .setDestinationInExternalFilesDir(null, "this-doesn't-really-exist.txt");
                        requestBatch.addRequest(request);
                        downloadManager.addCompletedBatch(requestBatch);
                    }
                }
        );
    }
}
