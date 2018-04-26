package com.novoda.downloadmanager;

public interface Builder {
    Builder withIdentifier(DownloadFileId downloadFileId);

    Builder saveTo(String path);

    Builder saveTo(String path, String fileName);

    Batch.Builder apply();
}
