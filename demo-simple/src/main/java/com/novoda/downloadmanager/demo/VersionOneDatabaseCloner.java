package com.novoda.downloadmanager.demo;

import android.content.res.AssetManager;

import com.novoda.notils.logger.simple.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class VersionOneDatabaseCloner {

    private final AssetManager assetManager;

    VersionOneDatabaseCloner(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    boolean cloneDatabase() {
        String outputDatabasePath = "/data/data/com.novoda.downloadmanager.demo.simple/databases/";

        Log.d(getClass().getSimpleName(), "Copying database!");

        byte[] buffer = new byte[1024];
        OutputStream myOutput;
        int length;
        InputStream inputStream;
        File outputFile = new File(outputDatabasePath, "downloads.db");

        try {
            inputStream = assetManager.open("downloads.db");
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
