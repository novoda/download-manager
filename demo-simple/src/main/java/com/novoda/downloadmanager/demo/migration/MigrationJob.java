package com.novoda.downloadmanager.demo.migration;

import android.annotation.SuppressLint;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;

import com.novoda.downloadmanager.CompletedDownloadBatch;
import com.novoda.downloadmanager.CompletedDownloadFile;
import com.novoda.downloadmanager.DownloadManager;
import com.novoda.downloadmanager.FileSizeExtractor;
import com.novoda.downloadmanager.SqlDatabaseWrapper;
import com.novoda.downloadmanager.StorageRoot;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MigrationJob implements Runnable {

    private static final int RANDOMLY_CHOSEN_BUFFER_SIZE_THAT_SEEMS_TO_WORK = 4096;
    @SuppressLint("SdCardPath")
    private static final String V1_BASE_PATH = "/data/data/com.novoda.downloadmanager.demo.simple/files/Pictures/";

    private final File databaseFile;
    private final StorageRoot primaryStorageWithDownloadsSubpackage;
    private final StorageRoot primaryStorageWithPicturesSubpackage;
    private final DownloadManager downloadManager;
    private final Handler callbackHandler;
    private final MigrationJobCallback migrationJobCallback;

    public interface MigrationJobCallback {
        void onUpdate(String message);
    }

    public MigrationJob(File databaseFile,
                        StorageRoot primaryStorageWithDownloadsSubpackage,
                        StorageRoot primaryStorageWithPicturesSubpackage,
                        DownloadManager downloadManager,
                        Handler callbackHandler,
                        MigrationJobCallback migrationJobCallback) {
        this.databaseFile = databaseFile;
        this.primaryStorageWithDownloadsSubpackage = primaryStorageWithDownloadsSubpackage;
        this.primaryStorageWithPicturesSubpackage = primaryStorageWithPicturesSubpackage;
        this.downloadManager = downloadManager;
        this.callbackHandler = callbackHandler;
        this.migrationJobCallback = migrationJobCallback;
    }

    @Override
    public void run() {
        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(databaseFile.getAbsolutePath(), null, 0);
        SqlDatabaseWrapper database = new SqlDatabaseWrapper(sqLiteDatabase);
        PartialDownloadBatchesExtractor partialDownloadMigrationExtractor = new PartialDownloadBatchesExtractor(
                database,
                primaryStorageWithDownloadsSubpackage
        );

        FileSizeExtractor fileSizeExtractor = new FileSizeExtractor();
        CompletedDownloadBatchesExtractor migrationExtractor = new CompletedDownloadBatchesExtractor(
                database,
                V1_BASE_PATH,
                fileSizeExtractor,
                primaryStorageWithPicturesSubpackage
        );

        onUpdate("Extracting V1 downloads");
        List<VersionOnePartialDownloadBatch> partialDownloadBatches = partialDownloadMigrationExtractor.extractMigrations();
        List<CompletedDownloadBatch> completeDownloadBatches = migrationExtractor.extractMigrations();

        onUpdate("Queuing Partial Downloads");
        for (int i = partialDownloadBatches.size() - 1; i >= 0; i--) {
            VersionOnePartialDownloadBatch partialDownloadBatch = partialDownloadBatches.get(i);
            downloadManager.download(partialDownloadBatch.batch());
            partialDownloadBatches.remove(i);

            for (String originalFileLocation : partialDownloadBatch.originalFileLocations()) {
                boolean usedInFurtherBatches = originalFileUsedInFurtherBatches(
                        originalFileLocation,
                        partialDownloadBatches,
                        completeDownloadBatches
                );
                if (!usedInFurtherBatches) {
                    deleteVersionOneFile(originalFileLocation);
                }
            }
        }

        onUpdate("Migrating Complete Downloads");

        for (int i = completeDownloadBatches.size() - 1; i >= 0; i--) {
            CompletedDownloadBatch completedDownloadBatch = completeDownloadBatches.get(i);
            downloadManager.addCompletedBatch(completedDownloadBatch);
            completeDownloadBatches.remove(i);

            for (CompletedDownloadFile completedDownloadFile : completedDownloadBatch.completedDownloadFiles()) {
                migrateV1FileToV2Location(completedDownloadFile);

                boolean usedInFurtherBatches = originalFileUsedInFurtherBatches(
                        completedDownloadFile.originalFileLocation(),
                        partialDownloadBatches,
                        completeDownloadBatches
                );
                if (!usedInFurtherBatches) {
                    deleteVersionOneFile(completedDownloadFile.originalFileLocation());
                }
            }
        }

        onUpdate("Deleting V1 Database");
        database.deleteDatabase();
        onUpdate("Completed Migration");
    }

    private boolean originalFileUsedInFurtherBatches(String originalFile,
                                                     List<VersionOnePartialDownloadBatch> partialDownloadBatches,
                                                     List<CompletedDownloadBatch> completeDownloadBatches) {

        for (VersionOnePartialDownloadBatch partialDownloadBatch : partialDownloadBatches) {
            if (partialDownloadBatch.originalFileLocations().contains(originalFile)) {
                return true;
            }
        }

        for (CompletedDownloadBatch completeDownloadBatch : completeDownloadBatches) {
            for (CompletedDownloadFile completedDownloadFile : completeDownloadBatch.completedDownloadFiles()) {
                if (completedDownloadFile.originalFileLocation().equals(originalFile)) {
                    return true;
                }
            }
        }

        return false;
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

    // the resources are closed but PMD doesn't see it
    @SuppressWarnings("PMD.CloseResource")
    private void migrateV1FileToV2Location(CompletedDownloadFile completedDownloadFile) {
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            File originalFile = new File(completedDownloadFile.originalFileLocation());
            File newFile = new File(completedDownloadFile.newFileLocation());
            ensureParentDirectoriesExistFor(newFile);

            // open the v1 file
            outputStream = new FileOutputStream(newFile, true);
            inputStream = new FileInputStream(originalFile);
            byte[] bytes = new byte[RANDOMLY_CHOSEN_BUFFER_SIZE_THAT_SEEMS_TO_WORK];
            // read the v1 file
            int readLast = 0;
            while (readLast != -1) {
                readLast = inputStream.read(bytes);
                if (readLast != 0 && readLast != -1) {
                    // write the v1 file to the v2 location
                    outputStream.write(bytes, 0, readLast);
                }
            }
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), e.getMessage());
        } finally {
            closeStream(outputStream);
            closeStream(inputStream);
        }
    }

    private boolean ensureParentDirectoriesExistFor(File outputFile) {
        boolean parentExists = outputFile.getParentFile().exists();
        if (parentExists) {
            return true;
        }

        Log.w(getClass().getSimpleName(), String.format("path: %s doesn't exist, creating parent directories...", outputFile.getAbsolutePath()));
        return outputFile.getParentFile().mkdirs();
    }

    private void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Log.e(getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    private void onUpdate(String message) {
        callbackHandler.post(() -> migrationJobCallback.onUpdate(message));
    }
}
