package com.novoda.downloadmanager.demo;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.lib.Query;
import com.novoda.downloadmanager.lib.Request;

import java.util.ArrayList;
import java.util.List;

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

        setupDownloadingExample();

        final List<Download> downloads = new ArrayList<>();
        Cursor cursor = downloadManager.query(new Query());
        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE));
            String fileName = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_FILENAME));
            downloads.add(new Download(title, fileName));
        }

        ListView listView = (ListView) findViewById(R.id.main_downloads_list);
        listView.setAdapter(
                new BaseAdapter() {
                    @Override
                    public int getCount() {
                        return downloads.size();
                    }

                    @Override
                    public Download getItem(int position) {
                        return downloads.get(position);
                    }

                    @Override
                    public long getItemId(int position) {
                        return position;
                    }

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = View.inflate(parent.getContext(), R.layout.list_item_download, null);

                        Download download = getItem(position);
                        TextView titleTextView = (TextView) view.findViewById(R.id.download_title_text);
                        TextView locationTextView = (TextView) view.findViewById(R.id.download_location_text);

                        titleTextView.setText(download.getTitle());
                        locationTextView.setText(download.getFileName());

                        return view;
                    }
                });
    }

    private void setupDownloadingExample() {
        Uri uri = Uri.parse(BIG_FILE);
        final Request request = new Request(uri);
        request.setDestinationInInternalFilesDir(this, Environment.DIRECTORY_MOVIES, "podcast.mp3");
        request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setBigPictureUrl(BBC_COMEDY_IMAGE);
        request.setTitle("BBC Friday Night Comedy");
        request.setDescription("Nothing to do with beards.");
        request.setMimeType("audio/mp3");

        findViewById(R.id.main_download_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        long id = downloadManager.enqueue(request);
                        Log.d(TAG, "Download starting with id: " + id);
                    }
                });
    }

}
