package com.novoda.downloadmanager.lib;

import com.novoda.downloadmanager.lib.logger.LLog;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;

final class IOHelpers {

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
            LLog.e("Fail sync");
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
