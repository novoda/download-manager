package com.novoda.downloadmanager;

import android.content.Context;

class FilePersistenceFixtures {

    private FilePersistenceResult filePersistenceResult = FilePersistenceResult.SUCCESS;
    private boolean writeResult = true;
    private long currentSize = 100;
    private FilePersistenceType filePersistenceType = FilePersistenceType.INTERNAL;

    static FilePersistenceFixtures aFilePersistence() {
        return new FilePersistenceFixtures();
    }

    FilePersistenceFixtures withFilePersistenceResult(FilePersistenceResult filePersistenceResult) {
        this.filePersistenceResult = filePersistenceResult;
        return this;
    }

    FilePersistenceFixtures withWriteResult(boolean writeResult) {
        this.writeResult = writeResult;
        return this;
    }

    FilePersistenceFixtures withCurrentSize(long currentSize) {
        this.currentSize = currentSize;
        return this;
    }

    FilePersistenceFixtures withFilePersistenceType(FilePersistenceType filePersistenceType) {
        this.filePersistenceType = filePersistenceType;
        return this;
    }

    FilePersistence build() {
        return new FilePersistence() {
            @Override
            public void initialiseWith(Context context) {

            }

            @Override
            public FilePath basePath() {
                return FilePathCreator.create("foo/bar", "/");
            }

            @Override
            public FilePersistenceResult create(FilePath absoluteFilePath, FileSize fileSize) {
                return filePersistenceResult;
            }

            @Override
            public boolean write(byte[] buffer, int offset, int numberOfBytesToWrite) {
                return writeResult;
            }

            @Override
            public void delete(FilePath absoluteFilePath) {
                // do nothing.
            }

            @Override
            public long getCurrentSize(FilePath filePath) {
                return currentSize;
            }

            @Override
            public void close() {
                // do nothing.
            }

            @Override
            public FilePersistenceType getType() {
                return filePersistenceType;
            }
        };
    }
}
