package com.novoda.downloadmanager;

import android.content.Context;

public interface FilePersistence {

    void initialiseWith(Context context);

    FilePersistenceResult create(FilePath filePath, FileSize fileSize);

    boolean write(byte[] buffer, int offset, int numberOfBytesToWrite);

    void delete();

    long getCurrentSize();

    long getCurrentSize(FilePath filePath);

    void close();

    FilePersistenceType getType();
}
