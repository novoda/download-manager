package com.novoda.downloadmanager.lib;

import java.io.IOException;
import java.io.InputStream;

class TarTruncator implements DataTransferer {

    private static final byte BYTE_ZERO = 0x0;

    private final DataWriter dataWriter;

    public TarTruncator(DataWriter dataWriter) {
        this.dataWriter = dataWriter;
    }

    @Override
    public DownloadTask.State transferData(DownloadTask.State state, InputStream in) throws StopRequestException {
        DownloadTask.State newState = state;
        try {
            byte[] buffer = new byte[Constants.BUFFER_SIZE];
            int readLast;
            while ((readLast = readBuffer(in, buffer)) > 0 && isNotFullOfZeroes(buffer)) {
                newState = dataWriter.write(newState, buffer, readLast);
            }
            state.shouldPause = true;
            state.totalBytes = state.currentBytes;
            return newState;
        } catch (IOException e) {
            // It was doing the same thing in regular and exception cases
            return newState;
        }
    }

    private boolean isNotFullOfZeroes(byte[] buffer) {
        for (byte b : buffer) {
            if (b != BYTE_ZERO) {
                return true;
            }
        }
        return false;
    }

    private int readBuffer(InputStream fileInputStream, byte[] buffer) throws IOException {
        int read = 0;
        byte[] blockBuffer = new byte[Constants.BLOCK_SIZE];
        int readLast;
        while (read < Constants.BUFFER_SIZE && (readLast = readBlock(fileInputStream, blockBuffer)) > 0 && isNotFullOfZeroes(blockBuffer)) {
            System.arraycopy(blockBuffer, 0, buffer, read, readLast);
            read += readLast;
        }
        return read;
    }

    private int readBlock(InputStream fileInputStream, byte[] buffer) throws IOException {
        int read = 0;
        int readLast;
        while (read < Constants.BLOCK_SIZE) {
            readLast = fileInputStream.read(buffer, read, Constants.BLOCK_SIZE - read);
            if (readLast == Constants.NO_BYTES_READ) {
                return read;
            }
            read += readLast;
        }
        return read;
    }

}
