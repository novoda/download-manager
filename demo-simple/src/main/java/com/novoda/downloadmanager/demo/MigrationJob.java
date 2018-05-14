package com.novoda.downloadmanager.demo;

import android.annotation.SuppressLint;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.novoda.downloadmanager.CompletedDownloadBatch;
import com.novoda.downloadmanager.LiteDownloadManagerCommands;
import com.novoda.downloadmanager.MigrationExtractor;
import com.novoda.downloadmanager.PartialDownloadMigrationExtractor;
import com.novoda.downloadmanager.SqlDatabaseWrapper;
import com.novoda.downloadmanager.VersionOnePartialDownloadBatch;

import java.io.File;
import java.util.List;

class MigrationJob implements Runnable {

    @SuppressLint("SdCardPath")
    private static final String V1_BASE_PATH = "/data/data/com.novoda.downloadmanager.demo.simple/files/Pictures/";

    private final File databaseFile;
    private final LiteDownloadManagerCommands downloadManager;

    MigrationJob(File databaseFile, LiteDownloadManagerCommands downloadManager) {
        this.databaseFile = databaseFile;
        this.downloadManager = downloadManager;
    }

    @Override
    public void run() {
        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(databaseFile.getAbsolutePath(), null, 0);
        SqlDatabaseWrapper database = new SqlDatabaseWrapper(sqLiteDatabase);
        PartialDownloadMigrationExtractor partialDownloadMigrationExtractor = new PartialDownloadMigrationExtractor(database);

        MigrationExtractor migrationExtractor = new MigrationExtractor(database, V1_BASE_PATH);

        List<VersionOnePartialDownloadBatch> partialDownloadBatches = partialDownloadMigrationExtractor.extractMigrations();
        List<CompletedDownloadBatch> completeDownloadBatches = migrationExtractor.extractMigrations();

        for (VersionOnePartialDownloadBatch partialDownloadBatch : partialDownloadBatches) {
            downloadManager.download(partialDownloadBatch.batch());

            for (String originalFileLocation : partialDownloadBatch.originalFileLocations()) {
                deleteVersionOneFile(originalFileLocation);
            }
        }

        for (CompletedDownloadBatch completeDownloadBatch : completeDownloadBatches) {
            downloadManager.addCompletedBatch(completeDownloadBatch);

            for (CompletedDownloadBatch.CompletedDownloadFile completedDownloadFile : completeDownloadBatch.completedDownloadFiles()) {
                deleteVersionOneFile(completedDownloadFile.originalFileLocation());
            }
        }

        database.deleteDatabase();
    }

    private void deleteVersionOneFile(String originalFileLocation) {
        if (originalFileLocation != null && !originalFileLocation.isEmpty()) {
            File file = new File(originalFileLocation);
            boolean deleted = file.delete();
            if (!deleted) {
                String message = String.format("Could not delete File or Directory: %s", file.getPath());
                Log.e(getClass().getSimpleName(), message);
            }
        }
    }
}
