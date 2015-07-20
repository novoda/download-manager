package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.text.TextUtils;

import com.novoda.notils.logger.simple.Log;

import java.io.File;

class DownloadDeleter {

    private final ContentResolver resolver;

    public DownloadDeleter(ContentResolver resolver) {
        this.resolver = resolver;
    }

    public void deleteFileAndDatabaseRow(FileDownloadInfo info) {
        deleteFileAndMediaReference(info);
        resolver.delete(info.getAllDownloadsUri(), null, null);
    }

    public void deleteFileAndMediaReference(FileDownloadInfo info) {
        if (!TextUtils.isEmpty(info.getMediaProviderUri())) {
            resolver.delete(Uri.parse(info.getMediaProviderUri()), null, null);
        }

        if (!TextUtils.isEmpty(info.getFileName())) {
            deleteFileIfExists(info.getFileName());
            ContentValues blankData = new ContentValues();
            blankData.put(DownloadContract.Downloads.COLUMN_DATA, (String) null);
            resolver.update(info.getAllDownloadsUri(), blankData, null, null);
        }
    }

    private void deleteFileIfExists(String path) {
        if (!TextUtils.isEmpty(path)) {
            Log.d("deleteFileIfExists() deleting " + path);
            final File file = new File(path);
            if (file.exists() && !file.delete()) {
                Log.w("file: '" + path + "' couldn't be deleted");
            }
        }
    }

}
