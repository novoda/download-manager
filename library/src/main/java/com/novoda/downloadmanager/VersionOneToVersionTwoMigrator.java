package com.novoda.downloadmanager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class VersionOneToVersionTwoMigrator implements Migrator {

    private static final int BUFFER_SIZE = 8 * 512;

    private final RoomDownloadsPersistence downloadsPersistence;
    private final InternalFilePersistence internalFilePersistence;
    private final File databasePath;
    private final Callback migrationCompleteCallback;

    VersionOneToVersionTwoMigrator(RoomDownloadsPersistence downloadsPersistence,
                                   InternalFilePersistence internalFilePersistence,
                                   File databasePath,
                                   Callback migrationCompleteCallback) {
        this.downloadsPersistence = downloadsPersistence;
        this.internalFilePersistence = internalFilePersistence;
        this.databasePath = databasePath;
        this.migrationCompleteCallback = migrationCompleteCallback;
    }

    @Override
    public void migrate() {
        if (checkV1DatabaseExists()) {
            SQLiteDatabase database = SQLiteDatabase.openDatabase(databasePath.getAbsolutePath(), null, 0);
            List<Migration> migrations = extractMigrationsFrom(database);

            migrateV1FilesToV2Location(migrations);
            migrateV1DataToV2Database(migrations);

            deleteFrom(database, migrations);
            database.close();

            migrationCompleteCallback.onMigrationComplete();
        } else {
            Log.d("MainActivity", "downloads.db doesn't exist!");
        }
    }

    private boolean checkV1DatabaseExists() {
        return databasePath.exists();
    }

    private List<Migration> extractMigrationsFrom(SQLiteDatabase database) {
        Cursor batchesCursor = database.rawQuery("SELECT _id, batch_title FROM batches", null);

        List<Migration> migrations = new ArrayList<>();
        while (batchesCursor.moveToNext()) {

            String query = "SELECT uri, _data, total_bytes FROM Downloads WHERE batch_id = ?";
            Cursor uriCursor = database.rawQuery(query, new String[]{batchesCursor.getString(0)});
            Batch.Builder newBatchBuilder = new Batch.Builder(DownloadBatchIdCreator.createFrom(batchesCursor.getString(0)), batchesCursor.getString(1));

            List<String> originalFileLocations = new ArrayList<>();
            List<FileSize> fileSizes = new ArrayList<>();

            while (uriCursor.moveToNext()) {
                Log.d("MainActivity", batchesCursor.getString(0) + " : " + batchesCursor.getString(1) + " : " + uriCursor.getString(0));
                newBatchBuilder.addFile(uriCursor.getString(0));

                String originalFileName = uriCursor.getString(1);
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

    private void migrateV1FilesToV2Location(List<Migration> migrations) {
        for (Migration migration : migrations) {
            migrateV1FilesToV2Location(migration);
        }
    }

    // TODO: create a map of the v1 filenames and v1 filesizes
    private void migrateV1FilesToV2Location(Migration migration) {
        Batch batch = migration.batch();

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

    private void migrateV1DataToV2Database(List<Migration> migrations) {
        for (Migration migration : migrations) {
            migrateV1DataToV2Database(migration);
        }
    }

    private void migrateV1DataToV2Database(Migration migration) {
        Batch batch = migration.batch();
        downloadsPersistence.startTransaction();

        DownloadBatchTitle downloadBatchTitle = new LiteDownloadBatchTitle(batch.getTitle());
        DownloadsBatchPersisted persistedBatch = new LiteDownloadsBatchPersisted(downloadBatchTitle, batch.getDownloadBatchId(), DownloadBatchStatus.Status.DOWNLOADED);
        downloadsPersistence.persistBatch(persistedBatch);

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
            downloadsPersistence.persistFile(persistedFile);
        }

        downloadsPersistence.transactionSuccess();
        downloadsPersistence.endTransaction();
    }

    private void deleteFrom(SQLiteDatabase database, List<Migration> migrations) {
        for (Migration migration : migrations) {
            String sql = "DELETE FROM batches WHERE _id = ?";
            Batch batch = migration.batch();
            String[] bindArgs = {batch.getDownloadBatchId().stringValue()};
            database.execSQL(sql, bindArgs);
        }
    }

}
