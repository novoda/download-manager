package com.novoda.downloadmanager;

import android.content.Context;
import android.support.annotation.FloatRange;

public interface FilePersistence {

    void initialiseWith(Context context, @FloatRange(from = 0.0, to = 0.5) StorageRequirementsRule storageRequirementsRule);

    FilePath basePath();

    FilePersistenceResult create(FilePath absoluteFilePath, FileSize fileSize);

    boolean write(byte[] buffer, int offset, int numberOfBytesToWrite);

    void delete(FilePath absoluteFilePath);

    long getCurrentSize(FilePath filePath);

    void close();

    FilePersistenceType getType();
}
