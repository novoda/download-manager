package com.novoda.downloadmanager.demo;

import android.content.Context;
import android.util.Log;

import com.novoda.downloadmanager.FilePath;
import com.novoda.downloadmanager.FilePersistence;
import com.novoda.downloadmanager.FilePersistenceResult;
import com.novoda.downloadmanager.FileSize;
import com.novoda.downloadmanager.StorageRequirementRule;

// Must be public
public class CustomFilePersistence implements FilePersistence {

    private static final String TAG = CustomFilePersistence.class.getSimpleName();

    private int currentSize;

    @Override
    public void initialiseWith(Context context, StorageRequirementRule storageRequirementRule) {
        Log.v(TAG, "initialise");
    }

    @Override
    public FilePersistenceResult create(FilePath absoluteFilePath, FileSize fileSize) {
        Log.v(TAG, "create " + absoluteFilePath.toString() + ", " + fileSize.toString());
        return FilePersistenceResult.SUCCESS;
    }

    @Override
    public boolean write(byte[] buffer, int offset, int numberOfBytesToWrite) {
        Log.v(TAG, "write offset: " + offset + ", numberOfBytesToWrite: " + numberOfBytesToWrite);
        currentSize = +numberOfBytesToWrite;
        return true;
    }

    @Override
    public void delete(FilePath absoluteFilePath) {
        Log.v(TAG, "delete: " + absoluteFilePath);
    }

    @Override
    public long getCurrentSize(FilePath filePath) {
        Log.v(TAG, "getCurrentSize for " + filePath + ": " + currentSize);
        return currentSize;
    }

    @Override
    public void close() {
        Log.v(TAG, "close");
    }
}
