package com.novoda.downloadmanager;

import android.content.Context;
import android.support.annotation.Nullable;

import com.novoda.downloadmanager.FilePersistenceResult.Status;
import com.novoda.notils.logger.simple.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

class InternalFilePersistence implements FilePersistence {

    private Context context;

    @Nullable
    private FileOutputStream fileOutputStream;
    @Nullable
    private FileName fileName;

    @Override
    public void initialiseWith(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public FilePath basePath() {
        return FilePathCreator.create(context.getFilesDir().getAbsolutePath(), "/");
    }

    @Override
    public FilePersistenceResult create(FilePath absoluteFilePath, FileSize fileSize) {
        if (fileSize.isTotalSizeUnknown()) {
            return FilePersistenceResult.newInstance(Status.ERROR_UNKNOWN_TOTAL_FILE_SIZE);
        }

        long usableSpace = context.getFilesDir().getUsableSpace();
        if (usableSpace < fileSize.totalSize()) {
            return FilePersistenceResult.newInstance(Status.ERROR_INSUFFICIENT_SPACE);
        }

        return create(absoluteFilePath);
    }

    private FilePersistenceResult create(FilePath absoluteFilePath) {
        if (absoluteFilePath.isUnknown()) {
            return FilePersistenceResult.newInstance(Status.ERROR_OPENING_FILE, absoluteFilePath);
        }

        try {
            File outputFile = new File(absoluteFilePath.path());
            boolean parentDirectoriesExist = ensureParentDirectoriesExistFor(outputFile);

            if (!parentDirectoriesExist) {
                return FilePersistenceResult.newInstance(Status.ERROR_OPENING_FILE, absoluteFilePath);
            }

            fileOutputStream = new FileOutputStream(outputFile, true);
        } catch (FileNotFoundException e) {
            Log.e(e, "File could not be opened");
            return FilePersistenceResult.newInstance(Status.ERROR_OPENING_FILE);
        }

        fileName = LiteFileName.from(getLocalNameFrom(absoluteFilePath));
        return FilePersistenceResult.newInstance(Status.SUCCESS, absoluteFilePath);
    }

    private boolean ensureParentDirectoriesExistFor(File outputFile) {
        boolean parentExists = outputFile.getParentFile().exists();
        if (parentExists) {
            return true;
        }

        Log.w(String.format("path: %s doesn't exist, creating parent directories...", outputFile.getAbsolutePath()));
        return outputFile.getParentFile().mkdirs();
    }

    private String getLocalNameFrom(FilePath filePath) {
        return new File(filePath.path()).getName();
    }

    @Override
    public boolean write(byte[] buffer, int offset, int numberOfBytesToWrite) {
        if (fileOutputStream == null) {
            Log.e("Cannot write, you must create the file first");
            return false;
        }

        try {
            fileOutputStream.write(buffer, offset, numberOfBytesToWrite);
            return true;
        } catch (IOException e) {
            Log.e(e, "Exception while writing to internal physical storage");
            return false;
        }
    }

    @Override
    public void delete() {
        if (fileName == null) {
            Log.w("Cannot delete, you must create the file first");
            return;
        }

        context.deleteFile(fileName.name());
    }

    @Override
    public long getCurrentSize() {
        if (fileOutputStream == null) {
            Log.e("Cannot get the current file size, you must create the file first");
            return 0;
        }

        try {
            return fileOutputStream.getChannel().size();
        } catch (IOException e) {
            Log.e(e, "Error requesting file size, make sure you create one first");
            return 0;
        }
    }

    @Override
    public long getCurrentSize(FilePath filePath) {
        FileOutputStream file = null;
        try {
            file = context.openFileOutput(getLocalNameFrom(filePath), Context.MODE_APPEND);
            return file.getChannel().size();
        } catch (IOException e) {
            Log.e(e, "Error requesting file size for " + fileName);
            return 0;
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    Log.e(e, "Error requesting file size for " + filePath);
                }
            }
        }
    }

    @Override
    public void close() {
        if (fileOutputStream == null) {
            return;
        }

        try {
            fileOutputStream.close();
        } catch (IOException e) {
            Log.e(e, "Failed to close: " + fileName);
        }
    }

    @Override
    public FilePersistenceType getType() {
        return FilePersistenceType.INTERNAL;
    }
}
