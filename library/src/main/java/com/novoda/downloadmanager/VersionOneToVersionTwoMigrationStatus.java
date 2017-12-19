package com.novoda.downloadmanager;

class VersionOneToVersionTwoMigrationStatus implements InternalMigrationStatus {

    private Status status;
    private int numberOfBatches;
    private int totalNumberOfBatches;
    private int percentageMigrated;

    VersionOneToVersionTwoMigrationStatus(Status status) {
        this.status = status;
    }

    @Override
    public void update(int currentBatch, int numberOfBatches) {
        this.numberOfBatches = currentBatch;
        this.totalNumberOfBatches = numberOfBatches;
        this.percentageMigrated = getPercentageFrom(currentBatch, numberOfBatches);
    }

    private int getPercentageFrom(int numberOfBatches, int totalNumberOfBatches) {
        return (int) ((((float) numberOfBatches) / ((float) totalNumberOfBatches)) * 100);
    }

    @Override
    public void markAsExtracting() {
        status = Status.EXTRACTING;
    }

    @Override
    public void markAsMigrating() {
        status = Status.MIGRATING_FILES;
    }

    @Override
    public void markAsDeleting() {
        status = Status.DELETING_V1_DATABASE;
    }

    @Override
    public void markAsComplete() {
        status = Status.COMPLETE;
    }

    @Override
    public int numberOfMigratedBatches() {
        return numberOfBatches;
    }

    @Override
    public int totalNumberOfBatchesToMigrate() {
        return totalNumberOfBatches;
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
