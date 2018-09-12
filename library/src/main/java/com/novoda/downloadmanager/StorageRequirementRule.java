package com.novoda.downloadmanager;

import java.io.File;

/**
 * Used to define the rules that need to be satisfied to download an asset.
 * Clients of this library can create their own custom implementations and
 * pass them to {@link DownloadManagerBuilder#withStorageRequirementRules(StorageRequirementRule...)}.
 */
public interface StorageRequirementRule {

    /**
     * Determines whether a {@link File} has violated a storage requirement rule.
     *
     * @param storageDirectory of the file to check.
     * @param downloadFileSize of the file to check.
     * @return whether the {@link File} has violated the rule.
     */
    boolean hasViolatedRule(File storageDirectory, FileSize downloadFileSize);

}
