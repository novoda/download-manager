package com.novoda.downloadmanager;

import java.io.File;

public interface StorageRequirementRule {

    boolean hasViolatedRule(File storageDirectory, FileSize downloadFileSize);

}
