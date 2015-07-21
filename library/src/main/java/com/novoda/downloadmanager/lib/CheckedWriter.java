package com.novoda.downloadmanager.lib;

import java.io.IOException;
import java.io.OutputStream;

public class CheckedWriter implements DataWriter {

    private final StorageManager storageManager;
    private final int destination;
    private final String fileName;
    private final OutputStream outputStream;

    public CheckedWriter(StorageManager storageManager, int destination, String fileName, OutputStream outputStream) {
        this.storageManager = storageManager;
        this.destination = destination;
        this.fileName = fileName;
        this.outputStream = outputStream;
    }

    @Override
    public DownloadThread.State write(DownloadThread.State state, byte[] buffer, int count) throws StopRequestException {

        storageManager.verifySpaceBeforeWritingToFile(destination, fileName, count);

        boolean forceVerified = false;
        while (true) {
            try {
                state.gotData = true;
                outputStream.write(buffer, 0, count);
                state.currentBytes += count;
                return state;
            } catch (IOException ex) {
                // TODO: better differentiate between DRM and disk failures
                if (!forceVerified) {
                    // couldn't write to file. are we out of space? check.
                    storageManager.verifySpace(destination, fileName, count);
                    forceVerified = true;
                } else {
                    throw new StopRequestException(
                            DownloadStatus.FILE_ERROR,
                            "Failed to write data: " + ex);
                }
            }
        }
    }
}
