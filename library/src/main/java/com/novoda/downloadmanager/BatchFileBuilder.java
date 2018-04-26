package com.novoda.downloadmanager;

public interface BatchFileBuilder {
    BatchFileBuilder withIdentifier(DownloadFileId downloadFileId);

    BatchFileBuilder saveTo(String path);

    BatchFileBuilder saveTo(String path, String fileName);

    BatchBuilder apply();
}
