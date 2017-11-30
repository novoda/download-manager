package com.novoda.downloadmanager.demo.simple;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.facebook.stetho.Stetho;
import com.novoda.downloadmanager.AllBatchStatusesCallback;
import com.novoda.downloadmanager.Batch;
import com.novoda.downloadmanager.DownloadBatchId;
import com.novoda.downloadmanager.DownloadBatchIdCreator;
import com.novoda.downloadmanager.DownloadBatchStatus;
import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.FileName;
import com.novoda.downloadmanager.FileSize;
import com.novoda.downloadmanager.InternalFilePersistence;
import com.novoda.downloadmanager.LiteDownloadManagerCommands;
import com.novoda.downloadmanager.LiteFileName;
import com.novoda.downloadmanager.LiteFileSize;
import com.novoda.downloadmanager.demo.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String BIG_FILE = "http://ipv4.download.thinkbroadband.com/200MB.zip";
    //    private static final String PENGUINS_IMAGE = "http://i.imgur.com/Y7pMO5Kb.jpg";
    private static final DownloadBatchId BEARD_ID = DownloadBatchIdCreator.createFrom("beard_id");
    private static final int BUFFER_SIZE = 8 * 512;

    private LiteDownloadManagerCommands downloadManagerCommands;
    private RecyclerView recyclerView;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Stetho.initializeWithDefaults(this);
        setContentView(R.layout.activity_main);
        emptyView = findViewById(R.id.main_no_downloads_view);
        recyclerView = findViewById(R.id.main_downloads_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Handler handler = new Handler(Looper.getMainLooper());
        downloadManagerCommands = DownloadManagerBuilder
                .newInstance(this, handler, R.mipmap.ic_launcher)
                .build();

        setupDownloadingExample();
        setupQueryingExample();
    }

    private void migrateV1DownloadsUsingNewThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                moveV1FilesToV2Location();
            }
        }).start();
    }

    private void moveV1FilesToV2Location() {
        if (checkV1DatabaseExists()) {
            File dbFile = this.getDatabasePath("downloads.db");

            SQLiteDatabase database = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, 0);

            Cursor cursor = database.rawQuery("SELECT * FROM Downloads", null);
            Cursor anotherCursor = database.rawQuery("SELECT * FROM DownloadsByBatch", null);
            cursor.moveToFirst();
            anotherCursor.moveToFirst();
            
            String fileName = cursor.getString(cursor.getColumnIndex("_data"));
            long fileSize = cursor.getLong(cursor.getColumnIndex("total_bytes"));
            String fileUri = cursor.getString(cursor.getColumnIndex("uri"));
            String title = anotherCursor.getString(anotherCursor.getColumnIndex("batch_title"));

            cursor.close();
            anotherCursor.close();
            database.close();

            Batch batch = new Batch.Builder(BEARD_ID, title)
                    .addFile(fileUri)
                    .build();

            Map<FileName, FileSize> map = new HashMap<>();
            for (String uri : batch.getFileUrls()) {
                FileName newFileName = LiteFileName.from(batch, uri);
                FileSize newFileSize = new LiteFileSize(fileSize, fileSize);
                map.put(newFileName, newFileSize);
            }

            InternalFilePersistence internalFilePersistence = new InternalFilePersistence();
            internalFilePersistence.initialiseWith(this);

            for (Map.Entry<FileName, FileSize> nameAndSize : map.entrySet()) {
                FileName actualFileName = nameAndSize.getKey();
                FileSize actualFileSize = nameAndSize.getValue();
                internalFilePersistence.create(actualFileName, actualFileSize);

                FileInputStream inputStream = null;
                try {
                    // open the v1 file
                    File file = new File(fileName);

                    for (File file1 : getFilesDir().listFiles()) {
                        Log.d("MainActivity", "File Name: " + file1.getName() + ", File size: " + file1.length());
                    }

                    inputStream = new FileInputStream(file);

                    byte[] bytes = new byte[BUFFER_SIZE];

                    int readLast = 0;
                    while (readLast != -1) {
                        readLast = inputStream.read(bytes);
                        if (readLast != 0 && readLast != -1) {
                            internalFilePersistence.write(bytes, 0, readLast);
                            //Log.d("MainActivity", Arrays.toString(bytes));
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        internalFilePersistence.close();
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            Log.d("MainActivity", "downloads.db doesn't exist!");
        }
    }

    private boolean checkV1DatabaseExists() {
        File dbFile = this.getDatabasePath("downloads.db");
        return dbFile.exists();
    }

    private void setupDownloadingExample() {
        findViewById(R.id.main_download_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(@NonNull View v) {
                        final Batch batch = new Batch.Builder(BEARD_ID, "Family of Penguins")
                                .addFile(BIG_FILE)
                                .build();
                        downloadManagerCommands.download(batch);
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
                        migrateV1DownloadsUsingNewThread();
                    }
                }
        );
    }

    private void queryForDownloads() {
        downloadManagerCommands.getAllDownloadBatchStatuses(new AllBatchStatusesCallback() {
            @Override
            public void onReceived(List<DownloadBatchStatus> downloadBatchStatuses) {
                List<BeardDownload> beardDownloads = new ArrayList<>(downloadBatchStatuses.size());
                for (DownloadBatchStatus downloadBatchStatus : downloadBatchStatuses) {
                    BeardDownload beardDownload = new BeardDownload(downloadBatchStatus.getDownloadBatchTitle(), downloadBatchStatus.status());
                    beardDownloads.add(beardDownload);
                }
                onQueryResult(beardDownloads);
            }
        });
    }

    public void onQueryResult(List<BeardDownload> beardDownloads) {
        recyclerView.setAdapter(new BeardDownloadAdapter(beardDownloads));
        emptyView.setVisibility(beardDownloads.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
