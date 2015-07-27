package com.novoda.downloadmanager.lib;

import java.io.IOException;
import java.io.InputStream;

class RegularDataTransferer implements DataTransferer {

    private final DataWriter dataWriter;

    public RegularDataTransferer(DataWriter dataWriter) {
        this.dataWriter = dataWriter;
    }

    @Override
    public DownloadTask.State transferData(DownloadTask.State state, InputStream in) throws StopRequestException {
        DownloadTask.State newState = state;
        try {
            byte[] buffer = new byte[Constants.BUFFER_SIZE];
            int readLast = in.read(buffer);
            while (readLast != Constants.NO_BYTES_READ) {
                newState = dataWriter.write(newState, buffer, readLast);
                readLast = in.read(buffer);
            }
            return newState;
        } catch (IOException e) {
            // It was doing the same thing in regular and exception cases
            return newState;
        }
    }

}
