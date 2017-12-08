package com.novoda.downloadmanager.demo;

import android.content.res.AssetManager;

import com.novoda.notils.logger.simple.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class VersionOneDatabaseCloner {

    private static final String DATABASE_PATH = "/data/data/com.novoda.downloadmanager.demo.simple/databases/";
    private static final String DATABASE_NAME = "downloads.db";

    private final AssetManager assetManager;

    VersionOneDatabaseCloner(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    boolean cloneDatabase() {
        Log.d(getClass().getSimpleName(), "Copying database!");

        byte[] buffer = new byte[1024];
        OutputStream myOutput;
        int length;
        InputStream inputStream;
        File outputFile = new File(DATABASE_PATH, DATABASE_NAME);

        try {
            inputStream = assetManager.open(DATABASE_NAME);
            myOutput = new FileOutputStream(outputFile);
            while ((length = inputStream.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.close();
            myOutput.flush();
            inputStream.close();

            Log.d(getClass().getSimpleName(), "Database has been copied!");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Failed to copy database.", e);
        }
        return false;
    }

}
