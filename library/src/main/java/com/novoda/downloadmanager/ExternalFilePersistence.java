package com.novoda.downloadmanager;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.novoda.downloadmanager.FilePersistenceResult.Status;
import com.novoda.notils.logger.simple.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

class ExternalFilePersistence implements FilePersistence {

    private static final String UNDEFINED_DIRECTORY_TYPE = null;
    private static final boolean APPEND = true;

    private Context context;

    @Nullable
    private FileOutputStream fileOutputStream;
    @Nullable
    private File file;

    @Override
    public void initialiseWith(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public FilePath basePath() {
        return FilePathCreator.create(getExternalFileDirWithBiggerAvailableSpace().getAbsolutePath(), "/");
    }

    @Override
    public FilePersistenceResult create(FilePath absoluteFilePath, FileSize fileSize) {
        if (fileSize.isTotalSizeUnknown()) {
            return FilePersistenceResult.newInstance(Status.ERROR_UNKNOWN_TOTAL_FILE_SIZE);
        }

        if (!isExternalStorageWritable()) {
            return FilePersistenceResult.newInstance(Status.ERROR_EXTERNAL_STORAGE_NON_WRITABLE);
        }

        File externalFileDir = getExternalFileDirWithBiggerAvailableSpace();

        long usableSpace = externalFileDir.getUsableSpace();
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
            file = new File(absoluteFilePath.path());
            boolean parentDirectoriesExist = ensureParentDirectoriesExistFor(file);

            if (!parentDirectoriesExist) {
                return FilePersistenceResult.newInstance(Status.ERROR_OPENING_FILE, absoluteFilePath);
            }

            fileOutputStream = new FileOutputStream(file, APPEND);
        } catch (FileNotFoundException e) {
            Log.e(e, "File could not be opened");
            return FilePersistenceResult.newInstance(Status.ERROR_OPENING_FILE);
        }

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

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private File getExternalFileDirWithBiggerAvailableSpace() {
        File externalFileDir = null;
        long usableSpace = 0;

        File[] externalFilesDirs = ContextCompat.getExternalFilesDirs(context, UNDEFINED_DIRECTORY_TYPE);
        for (File dir : externalFilesDirs) {
            if (dir == null) {
                continue;
            }

            long localUsableSpace = dir.getUsableSpace();
            if (usableSpace < localUsableSpace) {
                externalFileDir = dir;
                usableSpace = localUsableSpace;
            }
        }

        return externalFileDir;
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
        if (file == null) {
            Log.w("Cannot delete, you must create the file first");
            return;
        }

        boolean deleted = file.delete();

        String message = String.format("File or Directory: %s deleted: %s", file.getPath(), deleted);
        Log.d(getClass().getSimpleName(), message);
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
        try {
            FileOutputStream file = new FileOutputStream(filePath.path(), APPEND);
            return file.getChannel().size();
        } catch (IOException e) {
            Log.e(e, "Error requesting file size for " + filePath);
            return 0;
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
            Log.e(e, "Failed to close fileOutputStream.");
        }
    }

    @Override
    public FilePersistenceType getType() {
        return FilePersistenceType.EXTERNAL;
    }
}
