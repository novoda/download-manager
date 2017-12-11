package com.novoda.downloadmanager;

import java.util.List;

interface LocalFilesDirectory {
    List<String> contents();

    boolean deleteFile(String filename);
}
