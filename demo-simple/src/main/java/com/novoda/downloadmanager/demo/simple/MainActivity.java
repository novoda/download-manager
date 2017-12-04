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
import com.novoda.downloadmanager.DownloadBatchTitle;
import com.novoda.downloadmanager.DownloadFileId;
import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.DownloadsBatchPersisted;
import com.novoda.downloadmanager.DownloadsFilePersisted;
import com.novoda.downloadmanager.DownloadsPersistence;
import com.novoda.downloadmanager.FileName;
import com.novoda.downloadmanager.FilePath;
import com.novoda.downloadmanager.FilePathCreator;
import com.novoda.downloadmanager.FilePersistenceType;
import com.novoda.downloadmanager.FileSize;
import com.novoda.downloadmanager.InternalFilePersistence;
import com.novoda.downloadmanager.LiteDownloadBatchTitle;
import com.novoda.downloadmanager.LiteDownloadManagerCommands;
import com.novoda.downloadmanager.LiteDownloadsBatchPersisted;
import com.novoda.downloadmanager.LiteDownloadsFilePersisted;
import com.novoda.downloadmanager.LiteFileName;
import com.novoda.downloadmanager.LiteFileSize;
import com.novoda.downloadmanager.RoomDownloadsPersistence;
import com.novoda.downloadmanager.demo.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
                moveV1DownloadsToV2();
            }
        }).start();
    }

    private void moveV1DownloadsToV2() {
        if (checkV1DatabaseExists()) {
            File dbFile = this.getDatabasePath("downloads.db");
            SQLiteDatabase database = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, 0);
            Cursor batchesCursor = database.rawQuery("SELECT _id, batch_title FROM batches", null);

            while (batchesCursor.moveToNext()) {

                String query = "SELECT uri, _data, total_bytes FROM Downloads WHERE batch_id = ?";
                Cursor uriCursor = database.rawQuery(query, new String[]{batchesCursor.getString(0)});
                Batch.Builder newBatchBuilder = new Batch.Builder(DownloadBatchIdCreator.createFrom(batchesCursor.getString(0) + batchesCursor.getString(1)), batchesCursor.getString(1));

                Migration migration = new Migration();

                while (uriCursor.moveToNext()) {
                    Log.d("MainActivity", batchesCursor.getString(0) + " : " + batchesCursor.getString(1) + " : " + uriCursor.getString(0));
                    newBatchBuilder.addFile(uriCursor.getString(0));

                    String originalFileName = uriCursor.getString(1);
                    long rawFileSize = uriCursor.getLong(2);
                    FileSize fileSize = new LiteFileSize(rawFileSize, rawFileSize);
                    migration.add(originalFileName, fileSize);
                }

                uriCursor.close();

                Batch batch = newBatchBuilder.build();
                migration.setBatch(batch);

                migrateV1FilesToV2Location(migration);
                migrateV1DataToV2Database(migration);

                String sql = "DELETE FROM batches WHERE _id = ?";
                String[] bindArgs = {batchesCursor.getString(0)};
                database.execSQL(sql, bindArgs);
            }
            deleteDatabase("downloads.db");
            batchesCursor.close();
            database.close();
        } else {
            Log.d("MainActivity", "downloads.db doesn't exist!");
        }
    }

    // TODO: create a map of the v1 filenames and v1 filesizes
    private void migrateV1FilesToV2Location(Migration migration) {
        Batch batch = migration.batch();
        InternalFilePersistence internalFilePersistence = new InternalFilePersistence();
        internalFilePersistence.initialiseWith(this);

        for (int i = 0; i < migration.originalFileLocations().size(); i++) {
            // initialise the InternalFilePersistence
            String originalFileLocation = migration.originalFileLocations().get(i);
            FileSize actualFileSize = migration.fileSizes().get(i);
            FileName newFileName = LiteFileName.from(batch, batch.getFileUrls().get(i));
            internalFilePersistence.create(newFileName, actualFileSize);

            FileInputStream inputStream = null;
            try {
                // open the v1 file
                inputStream = new FileInputStream(new File(originalFileLocation));
                byte[] bytes = new byte[BUFFER_SIZE];

                // read the v1 file
                int readLast = 0;
                while (readLast != -1) {
                    readLast = inputStream.read(bytes);
                    if (readLast != 0 && readLast != -1) {
                        // write the v1 file to the v2 location
                        internalFilePersistence.write(bytes, 0, readLast);
                        bytes = new byte[BUFFER_SIZE];
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
    }

    private void migrateV1DataToV2Database(Migration migration) {
        Batch batch = migration.batch();
        DownloadsPersistence database = RoomDownloadsPersistence.newInstance(this);
        database.startTransaction();

        DownloadBatchTitle downloadBatchTitle = new LiteDownloadBatchTitle(batch.getTitle());
        DownloadsBatchPersisted persistedBatch = new LiteDownloadsBatchPersisted(downloadBatchTitle, batch.getDownloadBatchId(), DownloadBatchStatus.Status.DOWNLOADED);
        database.persistBatch(persistedBatch);

        for (int i = 0; i < migration.originalFileLocations().size(); i++) {
            String url = batch.getFileUrls().get(i);
            FileName fileName = LiteFileName.from(batch, url);
            FilePath filePath = FilePathCreator.create(fileName.name());
            DownloadFileId downloadFileId = DownloadFileId.from(batch);
            DownloadsFilePersisted persistedFile = new LiteDownloadsFilePersisted(
                    batch.getDownloadBatchId(),
                    downloadFileId,
                    fileName,
                    filePath,
                    migration.fileSizes().get(i).totalSize(),
                    url,
                    FilePersistenceType.INTERNAL
            );
            database.persistFile(persistedFile);
        }

        database.transactionSuccess();
        database.endTransaction();
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