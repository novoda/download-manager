package com.novoda.downloadmanager;

/**
 * Represents an absolute path on the system to the asset.
 */
public interface FilePath {

    String path();

    boolean isUnknown();
}
