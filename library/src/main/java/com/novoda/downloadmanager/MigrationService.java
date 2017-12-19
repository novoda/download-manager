package com.novoda.downloadmanager;

interface MigrationService {

    void updateNotification(NotificationInformation notificationInformation);

    void stackNotification(NotificationInformation notificationInformation);

    void updateMessage(MigrationStatus migrationStatus);

}
