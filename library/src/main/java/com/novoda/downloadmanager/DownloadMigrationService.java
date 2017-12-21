package com.novoda.downloadmanager;

interface DownloadMigrationService {

    void setNotificationChannelCreator(NotificationChannelCreator notificationChannelCreator);

    void setNotificationCreator(NotificationCreator<MigrationStatus> notificationCreator);

    void startMigration(MigrationCallback migrationCallback);

}
