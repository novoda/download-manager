package com.novoda.downloadmanager.demo;

import android.content.Context;

import com.novoda.downloadmanager.FilePath;
import com.novoda.downloadmanager.FilePathCreator;
import com.novoda.downloadmanager.FilePersistence;
import com.novoda.downloadmanager.FilePersistenceResult;
import com.novoda.downloadmanager.FilePersistenceType;
import com.novoda.downloadmanager.FileSize;
import com.novoda.downloadmanager.Logger;

// Must be public
public class CustomFilePersistence implements FilePersistence {

    private int currentSize;

    @Override
    public void initialiseWith(Context context) {
        Logger.v("initialise");
    }

    @Override
    public FilePath basePath() {
        FilePath filePath = FilePathCreator.unknownFilePath();
        Logger.v("basePath " + filePath.toString());
        return filePath;
    }

    @Override
    public FilePersistenceResult create(FilePath absoluteFilePath, FileSize fileSize) {
        Logger.v("create " + absoluteFilePath.toString() + ", " + fileSize.toString());
        return FilePersistenceResult.SUCCESS;
    }

    @Override
    public boolean write(byte[] buffer, int offset, int numberOfBytesToWrite) {
        Logger.v("write offset: " + offset + ", numberOfBytesToWrite: " + numberOfBytesToWrite);
        currentSize = +numberOfBytesToWrite;
        return true;
    }

    @Override
    public void delete(FilePath absoluteFilePath) {
        Logger.v("delete: " + absoluteFilePath);
    }

    @Override
    public long getCurrentSize(FilePath filePath) {
        Logger.v("getCurrentSize for " + filePath + ": " + currentSize);
        return currentSize;
    }

    @Override
    public void close() {
        Logger.v("close");
    }

    @Override
    public FilePersistenceType getType() {
        return FilePersistenceType.CUSTOM;
    }
}
