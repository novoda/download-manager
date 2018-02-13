package com.novoda.downloadmanager;

interface DownloadMigrationService {

    void setNotificationChannelProvider(NotificationChannelProvider notificationChannelProvider);

    void setNotificationCreator(NotificationCreator<MigrationStatus> notificationCreator);

    void startMigration(String databaseFilename, MigrationCallback migrationCallback);
}
