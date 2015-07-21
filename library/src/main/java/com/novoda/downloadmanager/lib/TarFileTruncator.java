package com.novoda.downloadmanager.lib;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.novoda.downloadmanager.lib.IOHelpers.closeAfterWrite;
import static com.novoda.downloadmanager.lib.IOHelpers.closeQuietly;

class TarFileTruncator {

    private static final String COPY_SUFFIX = ".tmp";
    private static final byte BYTE_ZERO = 0x0;
    private static final int BLOCK_SIZE = 512;
    private static final int NO_BYTES_READ = -1;

    public long truncateIfNeeded(String fileOriginal) throws StopRequestException {
        String fileCopy = fileOriginal + COPY_SUFFIX;
        long newLength = copyFileTruncatingLastBytesIfNeeded(fileOriginal, fileCopy);
        replace(fileOriginal, fileCopy);
        return newLength;
    }

    private long copyFileTruncatingLastBytesIfNeeded(String fileOriginal, String fileCopy) throws StopRequestException {
        OutputStream out = null;
        FileDescriptor outFd = null;
        InputStream in = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(fileOriginal);
            FileOutputStream fileOutputStream = new FileOutputStream(fileCopy);
            out = fileOutputStream;
            outFd = fileOutputStream.getFD();
            in = fileInputStream;
            byte[] buffer = new byte[BLOCK_SIZE];
            int readLast;
            int readTotal = 0;
            do {
                readLast = readBlock(fileInputStream, buffer);
                if ((readLast == -1) || isFullOfZeroes(buffer)) {
                    return readTotal;
                }
                out.write(buffer, 0, readLast);
                readTotal += readLast;
            } while (readLast == BLOCK_SIZE);
            return readTotal;
        } catch (IOException e) {
            throw new StopRequestException(DownloadStatus.FILE_ERROR, e);
        } finally {
            closeAfterWrite(out, outFd);
            closeQuietly(in);
        }
    }

    private boolean isFullOfZeroes(byte[] buffer) {
        for (byte b : buffer) {
            if (b != BYTE_ZERO) {
                return false;
            }
        }
        return true;
    }

    private int readBlock(FileInputStream fileInputStream, byte[] buffer) throws IOException {
        int read = 0;
        int readLast;
        while (read < BLOCK_SIZE) {
            readLast = fileInputStream.read(buffer, read, BLOCK_SIZE - read);
            if (readLast == NO_BYTES_READ) {
                return read;
            }
            read += readLast;
        }
        return read;
    }

    private void replace(String fileOriginal, String fileCopy) {
        File oldFile = new File(fileOriginal);
        File newFile = new File(fileCopy);
        boolean deleted = oldFile.delete();
        boolean renamed = newFile.renameTo(oldFile);
        if (!deleted || !renamed) {
            throw new IllegalStateException("Could not replace file by truncated one");
        }
    }

}
