package com.novoda.downloadmanager;

import android.content.Context;

/**
 * For defining the mechanism by which files should be persisted on the device.
 */
public interface FilePersistence {

    void initialiseWith(Context context, StorageRequirementsRule storageRequirementsRule);

    FilePath basePath();

    FilePersistenceResult create(FilePath absoluteFilePath, FileSize fileSize);

    boolean write(byte[] buffer, int offset, int numberOfBytesToWrite);

    void delete(FilePath absoluteFilePath);

    long getCurrentSize(FilePath filePath);

    void close();

    FilePersistenceType getType();
}
