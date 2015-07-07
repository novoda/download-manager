/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.novoda.downloadmanager.lib;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import com.novoda.notils.logger.simple.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Manages the storage space consumed by Downloads Data dir. When space falls below
 * a threshold limit (set in resource xml files), starts cleanup of the Downloads data dir
 * to free up space.
 */
class StorageManager {
    /**
     * the max amount of space allowed to be taken up by the downloads data dir
     */
    private static final long sMaxdownloadDataDirSize = 100 * 1024 * 1024;

    /**
     * threshold (in bytes) beyond which the low space warning kicks in and attempt is made to
     * purge some downloaded files to make space
     */
    private static final long sDownloadDataDirLowSpaceThreshold = 10 * sMaxdownloadDataDirSize / 100;

    /**
     * see {@link Environment#getExternalStorageDirectory()}
     */
    private final File mExternalStorageDir;

    /**
     * see {@link android.os.Environment#getDataDirectory()}
     */
    private final File mInternalStorageDir;

    /**
     * see {@link Environment#getDownloadCacheDirectory()}
     */
    private final File mSystemCacheDir;

    /**
     * The downloaded files are saved to this dir. it is the value returned by
     * {@link Context#getCacheDir()}.
     */
    private final File mDownloadDataDir;

    /**
     * how often do we need to perform checks on space to make sure space is available
     */
    private static final int FREQUENCY_OF_CHECKS_ON_SPACE_AVAILABILITY = 1024 * 1024; // 1MB
    private int mBytesDownloadedSinceLastCheckOnSpace = 0;

    /**
     * misc members
     */
    private final Context mContext;
    private final Downloads downloads;

    public StorageManager(Context context, Downloads downloads) {
        mContext = context;
        this.downloads = downloads;
        mDownloadDataDir = getDownloadDataDirectory(context);
        mExternalStorageDir = Environment.getExternalStorageDirectory();
        mInternalStorageDir = Environment.getDataDirectory();
        mSystemCacheDir = Environment.getDownloadCacheDirectory();
        startThreadToCleanupDatabaseAndPurgeFileSystem();
    }

    /**
     * How often should database and filesystem be cleaned up to remove spurious files
     * from the file system and
     * The value is specified in terms of num of downloads since last time the cleanup was done.
     */
    private static final int FREQUENCY_OF_DATABASE_N_FILESYSTEM_CLEANUP = 250;
    private int mNumDownloadsSoFar = 0;

    synchronized void incrementNumDownloadsSoFar() {
        if (++mNumDownloadsSoFar % FREQUENCY_OF_DATABASE_N_FILESYSTEM_CLEANUP == 0) {
            startThreadToCleanupDatabaseAndPurgeFileSystem();
        }
    }

    /* start a thread to cleanup the following
     *      remove spurious files from the file system
     *      remove excess entries from the database
     */
    private Thread mCleanupThread = null;

    private synchronized void startThreadToCleanupDatabaseAndPurgeFileSystem() {
        if (mCleanupThread != null && mCleanupThread.isAlive()) {
            return;
        }
        mCleanupThread = new Thread() {
            @Override
            public void run() {
                removeSpuriousFiles();
                trimDatabase();
            }
        };
        mCleanupThread.start();
    }

    void verifySpaceBeforeWritingToFile(int destination, String path, long length)
            throws StopRequestException {
        // do this check only once for every 1MB of downloaded data
        if (incrementBytesDownloadedSinceLastCheckOnSpace(length) < FREQUENCY_OF_CHECKS_ON_SPACE_AVAILABILITY) {
            return;
        }
        verifySpace(destination, path, length);
    }

    void verifySpace(int destination, String path, long length) throws StopRequestException {
        resetBytesDownloadedSinceLastCheckOnSpace();
        File dir = null;
//        Log.i("in verifySpace, destination: " + destination + ", path: " + path + ", length: " + length);
        if (path == null) {
            throw new IllegalArgumentException("path can't be null");
        }
        switch (destination) {
            case Downloads.Impl.DESTINATION_CACHE_PARTITION:
            case Downloads.Impl.DESTINATION_CACHE_PARTITION_NOROAMING:
            case Downloads.Impl.DESTINATION_CACHE_PARTITION_PURGEABLE:
                dir = mDownloadDataDir;
                break;
            case Downloads.Impl.DESTINATION_EXTERNAL:
                dir = mExternalStorageDir;
                break;
            case Downloads.Impl.DESTINATION_SYSTEMCACHE_PARTITION:
                dir = mSystemCacheDir;
                break;
            case Downloads.Impl.DESTINATION_FILE_URI:
                if (path.startsWith(mExternalStorageDir.getPath())) {
                    dir = mExternalStorageDir;
                } else if (path.startsWith(mDownloadDataDir.getPath())) {
                    dir = mDownloadDataDir;
                } else if (path.startsWith(mSystemCacheDir.getPath())) {
                    dir = mSystemCacheDir;
                } else if (path.startsWith(mInternalStorageDir.getPath())) {
                    dir = mInternalStorageDir;
                }
                break;
        }
        if (dir == null) {
            throw new IllegalStateException("invalid combination of destination: " + destination + ", path: " + path);
        }
        findSpace(dir, length, destination);
    }

    /**
     * finds space in the given filesystem (input param: root) to accommodate # of bytes
     * specified by the input param(targetBytes).
     * returns true if found. false otherwise.
     */
    private synchronized void findSpace(File root, long targetBytes, int destination) throws StopRequestException {
        if (targetBytes == 0) {
            return;
        }
        if (destination == Downloads.Impl.DESTINATION_FILE_URI ||
                destination == Downloads.Impl.DESTINATION_EXTERNAL) {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                throw new StopRequestException(Downloads.Impl.STATUS_DEVICE_NOT_FOUND_ERROR, "external media not mounted");
            }
        }
        // is there enough space in the file system of the given param 'root'.
        long bytesAvailable = getAvailableBytesInFileSystemAtGivenRoot(root);
        if (bytesAvailable < sDownloadDataDirLowSpaceThreshold) {
            /* filesystem's available space is below threshold for low space warning.
             * threshold typically is 10% of download data dir space quota.
             * try to cleanup and see if the low space situation goes away.
             */
            discardPurgeableFiles(destination, sDownloadDataDirLowSpaceThreshold);
            removeSpuriousFiles();
            bytesAvailable = getAvailableBytesInFileSystemAtGivenRoot(root);
            if (bytesAvailable < sDownloadDataDirLowSpaceThreshold) {
                /*
                 * available space is still below the threshold limit.
                 *
                 * If this is system cache dir, print a warning.
                 * otherwise, don't allow downloading until more space
                 * is available because downloadmanager shouldn't end up taking those last
                 * few MB of space left on the filesystem.
                 */
                if (root.equals(mSystemCacheDir)) {
                    Log.w("System cache dir ('/cache') is running low on space." + "space available (in bytes): " + bytesAvailable);
                } else {
                    throw new StopRequestException(Downloads.Impl.STATUS_INSUFFICIENT_SPACE_ERROR,
                            "space in the filesystem rooted at: " + root + " is below 10% availability. stopping this download.");
                }
            }
        }
        if (root.equals(mDownloadDataDir)) {
            // this download is going into downloads data dir. check space in that specific dir.
            bytesAvailable = getAvailableBytesInDownloadsDataDir(mDownloadDataDir);
            if (bytesAvailable < sDownloadDataDirLowSpaceThreshold) {
                // print a warning
                Log.w("Downloads data dir: " + root + " is running low on space. space available (in bytes): " + bytesAvailable);
            }
            if (bytesAvailable < targetBytes) {
                // Insufficient space; make space.
                discardPurgeableFiles(destination, sDownloadDataDirLowSpaceThreshold);
                removeSpuriousFiles();
                bytesAvailable = getAvailableBytesInDownloadsDataDir(mDownloadDataDir);
            }
        }
        if (bytesAvailable < targetBytes) {
            throw new StopRequestException(Downloads.Impl.STATUS_INSUFFICIENT_SPACE_ERROR,
                    "not enough free space in the filesystem rooted at: " + root + " and unable to free any more");
        }
    }

    /**
     * returns the number of bytes available in the downloads data dir
     * TODO this implementation is too slow. optimize it.
     */
    private long getAvailableBytesInDownloadsDataDir(File root) {
        File[] files = root.listFiles();
        long space = sMaxdownloadDataDirSize;
        if (files == null) {
            return space;
        }
        int size = files.length;
        for (File file : files) {
            space -= file.length();
        }
        return space;
    }

    private long getAvailableBytesInFileSystemAtGivenRoot(File root) {
        StatFs stat = new StatFs(root.getPath());
        // put a bit of margin (in case creating the file grows the system by a few blocks)
        long availableBlocks = (long) stat.getAvailableBlocks() - 4;
        long size = stat.getBlockSize() * availableBlocks;
        return size;
    }

    File locateDestinationDirectory(String mimeType, int destination, long contentLength)
            throws StopRequestException {
        switch (destination) {
            case Downloads.Impl.DESTINATION_CACHE_PARTITION:
            case Downloads.Impl.DESTINATION_CACHE_PARTITION_PURGEABLE:
            case Downloads.Impl.DESTINATION_CACHE_PARTITION_NOROAMING:
                return mDownloadDataDir;
            case Downloads.Impl.DESTINATION_SYSTEMCACHE_PARTITION:
                return mSystemCacheDir;
            case Downloads.Impl.DESTINATION_EXTERNAL:
                File base = new File(mExternalStorageDir.getPath() + Constants.DEFAULT_DL_SUBDIR);
                if (!base.isDirectory() && !base.mkdir()) {
                    // Can't create download directory, e.g. because a file called "download"
                    // already exists at the root level, or the SD card filesystem is read-only.
                    throw new StopRequestException(Downloads.Impl.STATUS_FILE_ERROR, "unable to create external downloads directory " + base.getPath());
                }
                return base;
            default:
                throw new IllegalStateException("unexpected value for destination: " + destination);
        }
    }

    File getDownloadDataDirectory() {
        return mDownloadDataDir;
    }

    public static File getDownloadDataDirectory(Context context) {
        return context.getCacheDir();
    }

    /**
     * Deletes purgeable files from the cache partition. This also deletes
     * the matching database entries. Files are deleted in LRU order until
     * the total byte size is greater than targetBytes
     */
    private long discardPurgeableFiles(int destination, long targetBytes) {
        Log.i("discardPurgeableFiles: destination = " + destination + ", targetBytes = " + targetBytes);
        String destStr = (destination == Downloads.Impl.DESTINATION_SYSTEMCACHE_PARTITION) ?
                String.valueOf(destination) :
                String.valueOf(Downloads.Impl.DESTINATION_CACHE_PARTITION_PURGEABLE);
        String[] bindArgs = new String[]{destStr};
        Cursor cursor = mContext.getContentResolver().query(
                downloads.getAllDownloadsContentUri(),
                null,
                "( " +
                        Downloads.Impl.COLUMN_STATUS + " = '" + Downloads.Impl.STATUS_SUCCESS + "' AND " +
                        Downloads.Impl.COLUMN_DESTINATION + " = ? )",
                bindArgs,
                Downloads.Impl.COLUMN_LAST_MODIFICATION);
        if (cursor == null) {
            return 0;
        }
        long totalFreed = 0;
        try {
            final int dataIndex = cursor.getColumnIndex(Downloads.Impl._DATA);
            while (cursor.moveToNext() && totalFreed < targetBytes) {
                final String data = cursor.getString(dataIndex);
                if (TextUtils.isEmpty(data)) continue;

                File file = new File(data);
                Log.d("purging " + file.getAbsolutePath() + " for " + file.length() + " bytes");
                totalFreed += file.length();
                file.delete();
                long id = cursor.getLong(cursor.getColumnIndex(Downloads.Impl._ID));
                mContext.getContentResolver().delete(ContentUris.withAppendedId(downloads.getAllDownloadsContentUri(), id), null, null);
            }
        } finally {
            cursor.close();
        }
        Log.i("Purged files, freed " + totalFreed + " for " + targetBytes + " requested");
        return totalFreed;
    }

    /**
     * Removes files in the systemcache and downloads data dir without corresponding entries in
     * the downloads database.
     * This can occur if a delete is done on the database but the file is not removed from the
     * filesystem (due to sudden death of the process, for example).
     * This is not a very common occurrence. So, do this only once in a while.
     */
    private void removeSpuriousFiles() {
        Log.i("in removeSpuriousFiles");
        // get a list of all files in system cache dir and downloads data dir
        List<File> files = new ArrayList<File>();
        File[] listOfFiles = mSystemCacheDir.listFiles();
        if (listOfFiles != null) {
            files.addAll(Arrays.asList(listOfFiles));
        }
        listOfFiles = mDownloadDataDir.listFiles();
        if (listOfFiles != null) {
            files.addAll(Arrays.asList(listOfFiles));
        }
        if (files.size() == 0) {
            return;
        }
        Cursor cursor = mContext.getContentResolver()
                .query(downloads.getAllDownloadsContentUri(), new String[]{Downloads.Impl._DATA}, null, null, null);
        try {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String filename = cursor.getString(0);
                    if (!TextUtils.isEmpty(filename)) {
                        Log.v("in removeSpuriousFiles, preserving file " + filename);
                        files.remove(new File(filename));
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // delete files owned by us, but that don't appear in our database
        final int myUid = android.os.Process.myUid();
        for (File file : files) {
            final String path = file.getAbsolutePath();
//            try {
//                final StructStat stat = Libcore.os.stat(path);
//                if (stat.st_uid == myUid) {
            Log.v("deleting spurious file " + path);
            if (file.delete()) {
                Log.v("spurious file deleted");
            }
//                }
//            } catch (ErrnoException e) {
//                Log.w("stat(" + path + ") result: " + e);
//            }
        }
    }

    /**
     * Drops old rows from the database to prevent it from growing too large
     * TODO logic in this method needs to be optimized. maintain the number of downloads
     * in memory - so that this method can limit the amount of data read.
     */
    private void trimDatabase() {
        Log.i("in trimDatabase");
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(
                    downloads.getAllDownloadsContentUri(),
                    new String[]{Downloads.Impl._ID},
                    Downloads.Impl.COLUMN_STATUS + " >= '200'", null,
                    Downloads.Impl.COLUMN_LAST_MODIFICATION);
            if (cursor == null) {
                // This isn't good - if we can't do basic queries in our database, nothings gonna work
                Log.e("null cursor in trimDatabase");
                return;
            }
            if (cursor.moveToFirst()) {
                int numDelete = cursor.getCount() - Constants.MAX_DOWNLOADS;
                int columnId = cursor.getColumnIndexOrThrow(Downloads.Impl._ID);
                while (numDelete > 0) {
                    Uri downloadUri = ContentUris.withAppendedId(
                            downloads.getAllDownloadsContentUri(), cursor.getLong(columnId));
                    mContext.getContentResolver().delete(downloadUri, null, null);
                    if (!cursor.moveToNext()) {
                        break;
                    }
                    numDelete--;
                }
            }
        } catch (SQLiteException e) {
            // trimming the database raised an exception. alright, ignore the exception
            // and return silently. trimming database is not exactly a critical operation
            // and there is no need to propagate the exception.
            Log.w("trimDatabase failed with exception: " + e.getMessage());
            return;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private synchronized int incrementBytesDownloadedSinceLastCheckOnSpace(long val) {
        mBytesDownloadedSinceLastCheckOnSpace += val;
        return mBytesDownloadedSinceLastCheckOnSpace;
    }

    private synchronized void resetBytesDownloadedSinceLastCheckOnSpace() {
        mBytesDownloadedSinceLastCheckOnSpace = 0;
    }
}
