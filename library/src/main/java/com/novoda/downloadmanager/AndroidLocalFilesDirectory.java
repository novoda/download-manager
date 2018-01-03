package com.novoda.downloadmanager;

import android.content.Context;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class AndroidLocalFilesDirectory implements LocalFilesDirectory {

    private final Context context;

    AndroidLocalFilesDirectory(Context context) {
        this.context = context;
    }

    @Override
    public List<String> contents() {
        File filesDir = context.getFilesDir();
        if (filesDir != null) {
            String[] directoryAsStrings = filesDir.list();
            if (directoryAsStrings != null) {
                return Arrays.asList(directoryAsStrings);
            }
        }

        return Collections.emptyList();
    }

    @Override
    public boolean deleteFile(String filename) {
        return context.deleteFile(filename);
    }
}
