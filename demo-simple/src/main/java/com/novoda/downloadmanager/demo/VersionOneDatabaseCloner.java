package com.novoda.downloadmanager.demo;

import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.novoda.downloadmanager.SqlDatabaseWrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

class VersionOneDatabaseCloner {

    private static final String TAG = VersionOneDatabaseCloner.class.getSimpleName();
    private static final int BUFFER_SIZE = 1024;
    private static final byte[] BUFFER = new byte[BUFFER_SIZE];

    private static final String DATABASE_NAME = "downloads.db";
    private static final String DOWNLOAD_FILE_NAME_FORMAT = "%s.zip";
    private static final String ORIGINAL_FILE_LOCATION_QUERY = "SELECT _data FROM Downloads WHERE _data IS NOT NULL";
    private static final int ORIGINAL_FILE_LOCATION_COLUMN_INDEX = 0;

    private final AssetManager assetManager;
    private final Executor executor;
    private final CloneCallback cloneCallback;
    private final String originalDatabaseLocation;

    VersionOneDatabaseCloner(AssetManager assetManager,
                             Executor executor,
                             CloneCallback cloneCallback,
                             String originalDatabaseLocation) {
        this.assetManager = assetManager;
        this.executor = executor;
        this.cloneCallback = cloneCallback;
        this.originalDatabaseLocation = originalDatabaseLocation;
    }

    interface CloneCallback {
        void onUpdate(String updateMessage);
    }

    void cloneDatabaseWithDownloadSize(final String selectedFileSize) {
        executor.execute(() -> {
            cloneDatabase();
            cloneDownloadFilesWithSize(selectedFileSize);
            cloneCallback.onUpdate("Cloning Complete");
        });
    }

    private void cloneDatabase() {
        cloneCallback.onUpdate("Cloning Database...");
        File outputFile = new File(originalDatabaseLocation);
        createFileIfDoesNotExist(outputFile);
        copyAssetToFile(DATABASE_NAME, outputFile);
    }

    private void createFileIfDoesNotExist(File outputFile) {
        boolean parentPathDoesNotExist = !outputFile.getParentFile().exists();
        if (parentPathDoesNotExist) {
            Log.w(TAG, String.format("path: %s doesn't exist, creating parent directories...", outputFile.getAbsolutePath()));
            parentPathDoesNotExist = !outputFile.getParentFile().mkdirs();
        }

        if (parentPathDoesNotExist) {
            throw new IllegalArgumentException("Unable to create path: " + outputFile.getParentFile().getAbsolutePath());
        }
    }

    private void copyAssetToFile(String assetName, File outputFile) {
        InputStream inputStream = null;
        OutputStream myOutput = null;
        int length;
        try {
            inputStream = assetManager.open(assetName);
            myOutput = new FileOutputStream(outputFile, true);
            while ((length = inputStream.read(BUFFER)) > 0) {
                myOutput.write(BUFFER, 0, length);
            }
            Log.d(getClass().getSimpleName(), "Copied asset: " + assetName);
        } catch (IOException e) {
            Log.e(TAG, "Failed to copy asset: " + assetName, e);
        } finally {
            try {
                if (myOutput != null) {
                    myOutput.close();
                    myOutput.flush();
                }

                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to close streams.", e);
            }
        }
    }

    private void cloneDownloadFilesWithSize(String selectedFileSize) {
        cloneCallback.onUpdate("Cloning Files...");
        List<String> localFileLocations = localFileLocations();
        String fileName = String.format(DOWNLOAD_FILE_NAME_FORMAT, selectedFileSize);

        for (String localFileLocation : localFileLocations) {
            File outputFile = new File(localFileLocation);

            createFileIfDoesNotExist(outputFile);
            copyAssetToFile(fileName, outputFile);
        }
    }

    private List<String> localFileLocations() {
        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(originalDatabaseLocation, null, 0);
        SqlDatabaseWrapper database = new SqlDatabaseWrapper(sqLiteDatabase);
        Cursor originalFileLocationsCursor = database.rawQuery(ORIGINAL_FILE_LOCATION_QUERY);
        List<String> originalFileLocations = new ArrayList<>();

        while (originalFileLocationsCursor.moveToNext()) {
            String originalFileLocation = originalFileLocationsCursor.getString(ORIGINAL_FILE_LOCATION_COLUMN_INDEX);
            originalFileLocations.add(originalFileLocation);
        }
        database.close();
        return originalFileLocations;
    }

}
