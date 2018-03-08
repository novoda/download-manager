package com.novoda.downloadmanager;

class VersionOneToVersionTwoMigrationStatus implements InternalMigrationStatus {

    private static final int TOTAL_PERCENTAGE = 100;

    private final String migrationId;

    private Status status;
    private int numberOfMigrationsCompleted;
    private int totalNumberOfMigrations;
    private int percentageMigrated;

    VersionOneToVersionTwoMigrationStatus(String migrationId,
                                          Status status,
                                          int numberOfMigrationsCompleted,
                                          int totalNumberOfMigrations,
                                          int percentageMigrated) {
        this.migrationId = migrationId;
        this.status = status;
        this.numberOfMigrationsCompleted = numberOfMigrationsCompleted;
        this.totalNumberOfMigrations = totalNumberOfMigrations;
        this.percentageMigrated = percentageMigrated;
    }

    @Override
    public void update(int currentBatch, int numberOfBatches) {
        this.numberOfMigrationsCompleted = currentBatch;
        this.totalNumberOfMigrations = numberOfBatches;
        this.percentageMigrated = getPercentageFrom(currentBatch, numberOfBatches);
    }

    private int getPercentageFrom(int numberOfBatches, int totalNumberOfBatches) {
        return (int) ((((float) numberOfBatches) / ((float) totalNumberOfBatches)) * TOTAL_PERCENTAGE);
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
    public String migrationId() {
        return migrationId;
    }

    @Override
    public int numberOfMigratedBatches() {
        return numberOfMigrationsCompleted;
    }

    @Override
    public int totalNumberOfBatchesToMigrate() {
        return totalNumberOfMigrations;
    }

    @Override
    public int percentageMigrated() {
        return percentageMigrated;
    }

    @Override
    public Status status() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        VersionOneToVersionTwoMigrationStatus that = (VersionOneToVersionTwoMigrationStatus) o;

        if (numberOfMigrationsCompleted != that.numberOfMigrationsCompleted) {
            return false;
        }
        if (totalNumberOfMigrations != that.totalNumberOfMigrations) {
            return false;
        }
        if (percentageMigrated != that.percentageMigrated) {
            return false;
        }
        if (migrationId != null ? !migrationId.equals(that.migrationId) : that.migrationId != null) {
            return false;
        }
        return status == that.status;
    }

    @Override
    public int hashCode() {
        int result = migrationId != null ? migrationId.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + numberOfMigrationsCompleted;
        result = 31 * result + totalNumberOfMigrations;
        result = 31 * result + percentageMigrated;
        return result;
    }

    @Override
    public String toString() {
        return "VersionOneToVersionTwoMigrationStatus{" +
                "migrationId='" + migrationId + '\'' +
                ", status=" + status +
                ", numberOfMigrationsCompleted=" + numberOfMigrationsCompleted +
                ", totalNumberOfMigrations=" + totalNumberOfMigrations +
                ", percentageMigrated=" + percentageMigrated +
                '}';
    }
}
