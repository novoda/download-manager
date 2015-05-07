package com.novoda.downloadmanager.demo;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.lib.Request;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String BIG_FILE = "http://downloads.bbc.co.uk/podcasts/radio4/fricomedy/fricomedy_20150501-1855a.mp3";
    private static final String BBC_COMEDY_IMAGE = "http://ichef.bbci.co.uk/podcasts/artwork/266/fricomedy.jpg";

    private DownloadManager downloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.novoda.notils.logger.simple.Log.setShowLogs(true);
        setContentView(R.layout.activity_main);
        downloadManager = new DownloadManager(getContentResolver());

        Uri uri = Uri.parse(BIG_FILE);
        final Request request = new Request(uri);
        request.setDestinationInInternalFilesDir(this, Environment.DIRECTORY_MOVIES, "podcast.mp3");
        request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setBigPictureUrl(BBC_COMEDY_IMAGE);
        request.setTitle("BBC Friday Night Comedy");
        request.setDescription("Nothing to do with beards.");
        request.setMimeType("audio/mp3");

        findViewById(R.id.button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        long id = downloadManager.enqueue(request);
                        Log.d(TAG, "Download starting with id: " + id);
                    }
                });
    }

}
