package com.novoda.downloadmanager;

import android.content.Context;

public interface FilePersistence {

    void initialiseWith(Context context);

    FilePath basePath();

    FilePersistenceResult create(FilePath absoluteFilePath, FileSize fileSize);

    boolean write(byte[] buffer, int offset, int numberOfBytesToWrite);

    void delete(FilePath absoluteFilePath);

    long getCurrentSize(FilePath filePath);

    void close();

    FilePersistenceType getType();
}
