package com.novoda.downloadmanager.task;

import java.io.IOException;
import java.io.OutputStream;

class CheckedWriter implements DataWriter {

    private final SpaceVerifier spaceVerifier;
    private final OutputStream outputStream;

    public CheckedWriter(SpaceVerifier spaceVerifier, OutputStream outputStream) {
        this.spaceVerifier = spaceVerifier;
        this.outputStream = outputStream;
    }

    @Override
    public State write(State state, byte[] buffer, int count) {

        spaceVerifier.verifySpacePreemptively(count);

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
                    spaceVerifier.verifySpace(count);
                    forceVerified = true;
                } else {
//                    throw new StopRequestException(
//                            DownloadStatus.FILE_ERROR,
//                            "Failed to write data: " + ex);
                }
            }
        }
    }
}