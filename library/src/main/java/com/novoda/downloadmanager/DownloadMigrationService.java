package com.novoda.downloadmanager;

interface DownloadMigrationService {

    void setNotificationMetadata(NotificationMetadata<MigrationStatus> notificationMetadata);

    void startMigration(MigrationCallback migrationCallback);
}
