package com.novoda.downloadmanager;

interface DownloadMigrationService {

    void setMigrationCallback(MigrationServiceBinder.Callback migrationCallback);

    void setNotificationChannelCreator(NotificationChannelCreator notificationChannelCreator);

    void setNotificationCreator(NotificationCreator<MigrationStatus> notificationCreator);

    void startMigration();
}
