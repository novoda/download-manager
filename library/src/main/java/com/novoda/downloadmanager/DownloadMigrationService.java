package com.novoda.downloadmanager;

interface DownloadMigrationService {

    void setNotificationCreator(NotificationCreator<MigrationStatus> notificationCreator);

    void startMigration(MigrationCallback migrationCallback);
}
