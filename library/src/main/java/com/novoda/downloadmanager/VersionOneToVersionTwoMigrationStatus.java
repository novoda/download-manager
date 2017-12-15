package com.novoda.downloadmanager;

class VersionOneToVersionTwoMigrationStatus implements MigrationStatus {

    private int percentageMigrated;
    private Status status;

    VersionOneToVersionTwoMigrationStatus(int percentageMigrated, Status status) {
        this.percentageMigrated = percentageMigrated;
        this.status = status;
    }

    @Override
    public int percentageMigrated() {
        return percentageMigrated;
    }

    @Override
    public Status status() {
        return status;
    }
}
