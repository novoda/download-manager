package com.novoda.downloadmanager.lib;

import java.io.IOException;
import java.io.InputStream;

public class TarTruncator implements DataTransferer {

    private static final byte BYTE_ZERO = 0x0;
    private static final int BLOCK_SIZE = 512;
    private static final int NO_BYTES_READ = -1;

    private final DataWriter dataWriter;

    public TarTruncator(DataWriter dataWriter) {
        this.dataWriter = dataWriter;
    }

    @Override
    public DownloadThread.State transferData(DownloadThread.State state, InputStream in) throws StopRequestException {
        DownloadThread.State newState = state;
        try {
            byte[] buffer = new byte[BLOCK_SIZE];
            int readLast = readBlock(in, buffer);
            while (readsData(readLast) && isNotFullOfZeroes(buffer)) {
                newState = dataWriter.write(newState, buffer, readLast);
                readLast = readBlock(in, buffer);
            }
            state.shouldPause = true;
            state.totalBytes = state.currentBytes;
            return newState;
        } catch (IOException e) {
            // It was doing the same thing in regular and exception cases
            return newState;
        }
    }

    private boolean readsData(int readLast) {
        return readLast != -1 && readLast != 0;
    }

    private boolean isNotFullOfZeroes(byte[] buffer) {
        for (byte b : buffer) {
            if (b != BYTE_ZERO) {
                return true;
            }
        }
        return false;
    }

    private int readBlock(InputStream fileInputStream, byte[] buffer) throws IOException {
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

}
