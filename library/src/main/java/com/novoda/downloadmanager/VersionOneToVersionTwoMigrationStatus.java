package com.novoda.downloadmanager;

class VersionOneToVersionTwoMigrationStatus implements MigrationStatus {

    private final String message;

    VersionOneToVersionTwoMigrationStatus(String message) {
        this.message = message;
    }

    @Override
    public String message() {
        return message;
    }
}
