package com.novoda.downloadmanager.download;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import com.novoda.downloadmanager.Provider;
import com.novoda.downloadmanager.demo.simple.DB;
import com.novoda.downloadmanager.domain.Download;
import com.novoda.downloadmanager.domain.DownloadFile;
import com.novoda.downloadmanager.domain.DownloadId;
import com.novoda.downloadmanager.domain.DownloadRequest;
import com.novoda.downloadmanager.domain.DownloadStage;
import com.novoda.downloadmanager.domain.DownloadStatus;
import com.novoda.downloadmanager.domain.ExternalId;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class DatabaseInteraction {

    private final ContentResolver contentResolver;

    public DatabaseInteraction(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public DownloadId newDownloadRequest() {
        String requestTime = String.valueOf(System.nanoTime());
        insertRequest(requestTime);
        long downloadId = marshallDownloadIdFrom(requestTime);
        return new DownloadId(downloadId);
    }

    private long marshallDownloadIdFrom(String requestTime) {
        Cursor cursor = getRequestCursorFor(requestTime);
        cursor.moveToFirst();
        long downloadId = DB.Request.getId(cursor);
        cursor.close();
        return downloadId;
    }

    private Cursor getRequestCursorFor(String requestTime) {
        return contentResolver.query(
                Provider.REQUEST,
                null,
                DB.Columns.Request.RequestTimestamp + "=?",
                new String[]{requestTime},
                null
        );
    }

    private void insertRequest(String requestTime) {
        ContentValues values = new ContentValues();
        DB.Request.setRequestTimestamp(requestTime, values);
        contentResolver.insert(Provider.REQUEST, values);
    }

    public List<Download> getAllDownloads() {
        List<Download> downloads = new ArrayList<>();
        Cursor cursor = contentResolver.query(Provider.DOWNLOAD_WITH_SIZE, null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Download download = getDownload(cursor);
                    downloads.add(download);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return downloads;
    }

    private Download getDownload(Cursor cursor) {
        DownloadId id = new DownloadId(DB.Download.getDownloadId(cursor));

        long currentSize = Long.valueOf(cursor.getString(cursor.getColumnIndex(DB.Columns.DownloadsWithSize.CurrentSize)));
        long totalSize = Long.valueOf(cursor.getString(cursor.getColumnIndex(DB.Columns.DownloadsWithSize.TotalSize)));
        String identifier = cursor.getString(cursor.getColumnIndex(DB.Columns.DownloadsWithSize.DownloadIdentifier));
        DownloadStage downloadStage = DownloadStage.valueOf(DB.Download.getDownloadStage(cursor));
        DownloadStatus downloadStatus = DownloadStatus.from(downloadStage);
        List<DownloadFile> files = getFilesForId(id);
        return new Download(id, currentSize, totalSize, downloadStage, downloadStatus, files, new ExternalId(identifier));
    }

    private List<DownloadFile> getFilesForId(DownloadId id) {
        Cursor cursor = contentResolver.query(Provider.FILE, null, DB.Columns.File.FileDownloadId + "=?", new String[]{id.asString()}, null);
        cursor.moveToFirst();

        List<DownloadFile> files = new ArrayList<>();
        do {
            String upstreamUri = DB.File.getFileUri(cursor);
            int currentSize = DB.File.getFileCurrentSize(cursor);
            int totalSize = DB.File.getFileTotalSize(cursor);
            DownloadFile.FileStatus fileStatus = DownloadFile.FileStatus.valueOf(DB.File.getFileStatus(cursor));
            String localUri = DB.File.getFileLocalUri(cursor);
            String fileIdentifier = DB.File.getFileIdentifier(cursor);
            files.add(new DownloadFile(upstreamUri, currentSize, totalSize, localUri, fileStatus, fileIdentifier));
        } while (cursor.moveToNext());

        cursor.close();
        return files;
    }

    public void submitRequest(DownloadRequest downloadRequest) {
        ContentValues values = new ContentValues();
        long downloadId = downloadRequest.getId().asLong();
        DB.Download.setDownloadId((int) downloadId, values);
        DB.Download.setDownloadStage(DownloadStage.QUEUED.name(), values);
        DB.Download.setDownloadIdentifier(downloadRequest.getExternalId().asString(), values);
        contentResolver.insert(Provider.DOWNLOAD, values);

        ContentValues[] fileValues = createFileValues(downloadRequest.getFiles(), downloadId);
        contentResolver.bulkInsert(Provider.FILE, fileValues);
    }

    private ContentValues[] createFileValues(List<DownloadRequest.File> files, long downloadId) {
        ContentValues[] allValues = new ContentValues[files.size()];
        for (int index = 0; index < allValues.length; index++) {
            DownloadRequest.File file = files.get(index);
            ContentValues fileValues = new ContentValues();
            DB.File.setFileDownloadId((int) downloadId, fileValues);
            DB.File.setFileIdentifier(file.getIdentifier(), fileValues);
            DB.File.setFileUri(file.getUri(), fileValues);
            DB.File.setFileLocalUri(file.getLocalUri(), fileValues);
            DB.File.setFileStatus(DownloadFile.FileStatus.INCOMPLETE.name(), fileValues);

            allValues[index] = fileValues;
        }
        return allValues;
    }

    public void updateFileSize(DownloadFile file, long totalBytes) {
        ContentValues values = new ContentValues(1);
        DB.File.setFileTotalSize((int) totalBytes, values);
        contentResolver.update(Provider.FILE, values, DB.Columns.File.FileIdentifier + "=?", new String[]{file.getFileIdentifier()});
    }

    public void updateStatus(DownloadId downloadId, DownloadStage stage) {
        ContentValues values = new ContentValues(1);
        DB.Download.setDownloadStage(stage.name(), values);
        contentResolver.update(Provider.DOWNLOAD, values, DB.Columns.Download.DownloadId + "=?", new String[]{downloadId.asString()});
    }

    public void updateFileProgress(DownloadFile file, long bytesWritten) {
        ContentValues values = new ContentValues(1);
        DB.File.setFileCurrentSize((int) bytesWritten, values);
        contentResolver.update(Provider.FILE, values, DB.Columns.File.FileIdentifier + "=?", new String[]{file.getFileIdentifier()});
    }

    public void updateFile(DownloadFile file, DownloadFile.FileStatus status, long currentSize) {
        ContentValues values = new ContentValues(2);
        DB.File.setFileCurrentSize((int) currentSize, values);
        DB.File.setFileStatus(status.name(), values);
        contentResolver.update(Provider.FILE, values, DB.Columns.File.FileIdentifier + "=?", new String[]{file.getFileIdentifier()});
    }

    public Download getDownload(DownloadId id) {
        Cursor cursor = contentResolver.query(Provider.DOWNLOAD_WITH_SIZE, null, DB.Columns.Download.DownloadId + "=?", new String[]{id.asString()}, null);
        try {
            cursor.moveToFirst();
            return getDownload(cursor);
        } catch (Exception e) {
            throw new RuntimeException("get download failed", e);
        } finally {
            cursor.close();
        }
    }

    public void addCompletedRequest(DownloadRequest downloadRequest) {
        ContentValues values = new ContentValues();
        long downloadId = downloadRequest.getId().asLong();
        DB.Download.setDownloadId((int) downloadId, values);
        DB.Download.setDownloadStage(DownloadStage.COMPLETED.name(), values);
        contentResolver.insert(Provider.DOWNLOAD, values);

        ContentValues[] fileValues = createCompletedFileValues(downloadRequest.getFiles(), downloadId);
        contentResolver.bulkInsert(Provider.FILE, fileValues);
    }

    private ContentValues[] createCompletedFileValues(List<DownloadRequest.File> files, long downloadId) {
        ContentValues[] allValues = new ContentValues[files.size()];
        for (int index = 0; index < allValues.length; index++) {
            DownloadRequest.File requestFile = files.get(index);
            File file = new File(requestFile.getUri());
            ContentValues fileValues = new ContentValues();
            DB.File.setFileDownloadId((int) downloadId, fileValues);
            DB.File.setFileIdentifier(requestFile.getIdentifier(), fileValues);
            DB.File.setFileUri(requestFile.getUri(), fileValues);
            DB.File.setFileLocalUri(requestFile.getLocalUri(), fileValues);
            DB.File.setFileStatus(DownloadFile.FileStatus.COMPLETE.name(), fileValues);
            DB.File.setFileTotalSize((int) file.length(), fileValues);
            DB.File.setFileCurrentSize((int) file.length(), fileValues);

            allValues[index] = fileValues;
        }
        return allValues;
    }

    public void delete(Download download) {
        long downloadId = download.getId().asLong();
        contentResolver.delete(Provider.DOWNLOAD, DB.Columns.Download.DownloadId + "=?", new String[]{String.valueOf(downloadId)});
        contentResolver.delete(Provider.FILE, DB.Columns.File.FileDownloadId + "=?", new String[]{String.valueOf(downloadId)});
    }
}
