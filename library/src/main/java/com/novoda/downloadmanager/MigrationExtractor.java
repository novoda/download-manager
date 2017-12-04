package com.novoda.downloadmanager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

class MigrationExtractor {

    List<Migration> extractMigrationsFrom(SQLiteDatabase database) {
        Cursor batchesCursor = database.rawQuery("SELECT _id, batch_title FROM batches", null);

        List<Migration> migrations = new ArrayList<>();
        while (batchesCursor.moveToNext()) {

            String query = "SELECT uri, _data, total_bytes FROM Downloads WHERE batch_id = ?";
            String batchId = batchesCursor.getString(0);
            String batchTitle = batchesCursor.getString(1);

            Cursor uriCursor = database.rawQuery(query, new String[]{batchId});
            Batch.Builder newBatchBuilder = new Batch.Builder(DownloadBatchIdCreator.createFrom(batchId), batchTitle);

            List<String> originalFileLocations = new ArrayList<>();
            List<FileSize> fileSizes = new ArrayList<>();

            while (uriCursor.moveToNext()) {
                String uri = uriCursor.getString(0);
                String originalFileName = uriCursor.getString(1);
                Log.d("MainActivity", batchId + " : " + batchTitle + " : " + uri);

                newBatchBuilder.addFile(uri);

                originalFileLocations.add(originalFileName);

                long rawFileSize = uriCursor.getLong(2);
                FileSize fileSize = new LiteFileSize(rawFileSize, rawFileSize);
                fileSizes.add(fileSize);
            }

            uriCursor.close();

            Batch batch = newBatchBuilder.build();
            migrations.add(new Migration(batch, originalFileLocations, fileSizes));
        }
        batchesCursor.close();
        return migrations;
    }

}
