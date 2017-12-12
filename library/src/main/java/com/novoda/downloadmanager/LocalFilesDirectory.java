package com.novoda.downloadmanager;

import java.util.List;

public interface LocalFilesDirectory {

    List<String> contents();

    boolean deleteFile(String filename);
}
