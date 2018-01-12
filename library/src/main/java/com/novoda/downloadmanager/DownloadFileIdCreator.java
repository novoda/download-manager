package com.novoda.downloadmanager;

public final class DownloadFileIdCreator {

    private DownloadFileIdCreator() {
        // non-instantiable class
    }

    public static DownloadFileId createFrom(String rawId) {
        return new LiteDownloadFileId(rawId);
    }
}
