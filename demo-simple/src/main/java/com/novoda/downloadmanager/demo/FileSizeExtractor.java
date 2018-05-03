package com.novoda.downloadmanager.demo;

import java.io.File;

public class FileSizeExtractor {

    public long extract(String path) {
        File file = new File(path);
        return file.length();
    }

}
