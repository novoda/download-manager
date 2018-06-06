package com.novoda.downloadmanager;

import java.io.File;

public class FileSizeExtractor {

    long fileSizeFor(String filePath) {
        File file = new File(filePath);
        return file.length();
    }
}
