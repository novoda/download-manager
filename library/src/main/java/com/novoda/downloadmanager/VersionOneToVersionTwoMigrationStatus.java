package com.novoda.downloadmanager;

class VersionOneToVersionTwoMigrationStatus implements InternalMigrationStatus {

    private static final int TOTAL_PERCENTAGE = 100;

    private final String migrationId;
    private final int totalNumberOfMigrations;

    private Status status;
    private int numberOfMigrationsCompleted;

    VersionOneToVersionTwoMigrationStatus(String migrationId,
                                          Status status,
                                          int numberOfMigrationsCompleted,
                                          int totalNumberOfMigrations) {
        this.migrationId = migrationId;
        this.status = status;
        this.numberOfMigrationsCompleted = numberOfMigrationsCompleted;
        this.totalNumberOfMigrations = totalNumberOfMigrations;
    }

    @Override
    public void onSingleBatchMigrated() {
        numberOfMigrationsCompleted++;
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
        return (int) ((((float) numberOfMigrationsCompleted) / ((float) totalNumberOfMigrations)) * TOTAL_PERCENTAGE);
    }

    @Override
    public Status status() {
        return status;
    }

    @Override
    public InternalMigrationStatus copy() {
        return new VersionOneToVersionTwoMigrationStatus(
                migrationId,
                status,
                numberOfMigrationsCompleted,
                totalNumberOfMigrations
        );
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
        return result;
    }

    @Override
    public String toString() {
        return "VersionOneToVersionTwoMigrationStatus{"
                + "migrationId='" + migrationId + '\''
                + ", status=" + status
                + ", numberOfMigrationsCompleted=" + numberOfMigrationsCompleted
                + ", totalNumberOfMigrations=" + totalNumberOfMigrations
                + '}';
    }
}
