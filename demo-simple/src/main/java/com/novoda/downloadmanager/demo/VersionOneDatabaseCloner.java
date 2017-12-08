package com.novoda.downloadmanager.demo;

import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.novoda.downloadmanager.SqlDatabaseWrapper;
import com.novoda.notils.exception.DeveloperError;
import com.novoda.notils.logger.simple.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

class VersionOneDatabaseCloner {

    private static final byte[] BUFFER = new byte[1024];

    private static final String DATABASE_PATH = "/data/data/com.novoda.downloadmanager.demo.simple/databases/";
    private static final String DATABASE_NAME = "downloads.db";
    private static final String DOWNLOAD_FILE_NAME = "file.zip";
    private static final String ORIGINAL_FILE_LOCATION_QUERY = "SELECT _data FROM Downloads WHERE _data IS NOT NULL";
    private static final int ORIGINAL_FILE_LOCATION_COLUMN_INDEX = 0;

    private final AssetManager assetManager;
    private final Executor executor;

    VersionOneDatabaseCloner(AssetManager assetManager, Executor executor) {
        this.assetManager = assetManager;
        this.executor = executor;
    }

    void cloneDatabase() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(getClass().getSimpleName(), "Copying database!");
                File outputFile = new File(DATABASE_PATH, DATABASE_NAME);
                copyAssetToFile(DATABASE_NAME, outputFile);

                Log.d(getClass().getSimpleName(), "Copying files!");
                cloneDownloadFiles();
                Log.d(getClass().getSimpleName(), "Finished Copying DB and Files!");
            }
        });
    }

    private void copyAssetToFile(String assetName, File outputFile) {
        InputStream inputStream;
        OutputStream myOutput;
        int length;
        try {
            inputStream = assetManager.open(assetName);
            myOutput = new FileOutputStream(outputFile, true);
            while ((length = inputStream.read(BUFFER)) > 0) {
                myOutput.write(BUFFER, 0, length);
            }
            myOutput.close();
            myOutput.flush();
            inputStream.close();

            Log.d(getClass().getSimpleName(), "Copied asset: " + assetName);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Failed to copy asset: " + assetName, e);
        }
    }

    private void cloneDownloadFiles() {
        List<String> localFileLocations = localFileLocations();

        for (String localFileLocation : localFileLocations) {
            File outputFile = new File(localFileLocation);

            createFileIfDoesNotExist(outputFile);
            copyAssetToFile(DOWNLOAD_FILE_NAME, outputFile);
        }

    }

    private void createFileIfDoesNotExist(File outputFile) {
        boolean parentPathDoesNotExist = !outputFile.getParentFile().exists();
        if (parentPathDoesNotExist) {
            Log.w(String.format("path: %s doesn't exist, creating parent directories...", outputFile.getAbsolutePath()));
            outputFile.getParentFile().mkdirs();
            parentPathDoesNotExist = !outputFile.getParentFile().exists();
        }

        if (parentPathDoesNotExist) {
            throw new DeveloperError("Unable to create path: " + outputFile.getParentFile().getAbsolutePath());
        }
    }

    private List<String> localFileLocations() {
        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(DATABASE_PATH + DATABASE_NAME, null, 0);
        SqlDatabaseWrapper database = new SqlDatabaseWrapper(sqLiteDatabase);
        Cursor originalFileLocationsCursor = database.rawQuery(ORIGINAL_FILE_LOCATION_QUERY);
        List<String> originalFileLocations = new ArrayList<>();

        while (originalFileLocationsCursor.moveToNext()) {
            String originalFileLocation = originalFileLocationsCursor.getString(ORIGINAL_FILE_LOCATION_COLUMN_INDEX);
            originalFileLocations.add(originalFileLocation);
        }
        sqLiteDatabase.close();
        return originalFileLocations;
    }

}
