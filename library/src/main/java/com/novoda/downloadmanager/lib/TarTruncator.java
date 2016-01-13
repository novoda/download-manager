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
            byte[] buffer = new byte[Constants.TAR_RECORD_SIZE];
            byte[] previousBuffer = new byte[Constants.TAR_RECORD_SIZE];
            byte[] swappingRef;
            int read;
            int previouslyRead = 0;

            while ((read = readRecord(in, buffer)) > 0) {
                newState = dataWriter.write(newState, previousBuffer, previouslyRead);
                swappingRef = previousBuffer;
                previousBuffer = buffer;
                buffer = swappingRef;
                previouslyRead = read;
            }

            newState = dataWriter.write(newState, previousBuffer, truncateEOFMarker(previousBuffer, previouslyRead));

            state.shouldPause = true;
            state.totalBytes = state.currentBytes;
            return newState;
        } catch (IOException e) {
            // It was doing the same thing in regular and exception cases
            return newState;
        }
    }

    private static int truncateEOFMarker(byte[] buffer, int length) {
        int position = length;

        while (position >= Constants.TAR_BLOCK_SIZE && isRangeFullOfZeroes(buffer, position - Constants.TAR_BLOCK_SIZE, position)) {
            position = position - Constants.TAR_BLOCK_SIZE;
        }

        return position;
    }

    private static boolean isRangeFullOfZeroes(byte[] buffer, int start, int end) {
        for (int i = start; i < end; i++) {
            if (buffer[i] != BYTE_ZERO) {
                return false;
            }
        }
        return true;
    }

    private static int readRecord(InputStream fileInputStream, byte[] buffer) throws IOException {
        int read = 0;
        int readLast;
        while (read < Constants.TAR_RECORD_SIZE) {
            readLast = fileInputStream.read(buffer, read, Constants.TAR_RECORD_SIZE - read);
            if (readLast == Constants.NO_BYTES_READ) {
                return read;
            }
            read += readLast;
        }
        return read;
    }

}
