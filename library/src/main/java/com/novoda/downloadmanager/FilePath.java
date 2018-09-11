package com.novoda.downloadmanager;

/**
 * Represents a path on the system to the asset.
 */
public interface FilePath {

    String path();

    boolean isUnknown();
}
