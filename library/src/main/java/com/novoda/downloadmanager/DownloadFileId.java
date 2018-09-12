package com.novoda.downloadmanager;

/**
 * Represents a unique identifier for a download file.
 * Clients should use {@link DownloadFileIdCreator} and pass to {@link BatchFileBuilder#withIdentifier(DownloadFileId)}
 * when adding files to a batch.
 */
public interface DownloadFileId {

    String rawId();
}
