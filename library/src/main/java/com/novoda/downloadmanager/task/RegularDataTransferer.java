package com.novoda.downloadmanager.task;

import java.io.IOException;
import java.io.InputStream;

class RegularDataTransferer implements DataTransferer {

    /**
     * The value representing the end of stream when, reading an InputStream
     */
    public static final int NO_BYTES_READ = -1;

    /**
     * The size of a tar block
     */
    public static final int TAR_BLOCK_SIZE = 512;

    /**
     * The buffer size used to stream the data
     */
    public static final int BUFFER_SIZE = 8 * TAR_BLOCK_SIZE;

    private final DataWriter dataWriter;



    public RegularDataTransferer(DataWriter dataWriter) {
        this.dataWriter = dataWriter;
    }

    @Override
    public State transferData(State state, InputStream in) {
        State newState = state;
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int readLast = in.read(buffer);
            while (readLast != NO_BYTES_READ) {
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