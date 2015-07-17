package com.novoda.downloadmanager.lib;

import com.novoda.notils.logger.simple.Log;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;

class IOHelpers {

    private IOHelpers() {
    }

    /**
     * Closes an outputStream and the FileDescriptor associated with it after ensuring all data as been written to disk.
     */
    static void closeAfterWrite(OutputStream out, FileDescriptor outFd) {
        try {
            if (out != null) {
                out.flush();
            }
            if (outFd != null) {
                outFd.sync();
            }
        } catch (IOException e) {
            Log.e("Fail sync");
        } finally {
            closeQuietly(out);
        }
    }

    /**
     * Closes a resource ignoring any error in the process.
     */
    static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

}
