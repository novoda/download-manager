package com.novoda.downloadmanager;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

public interface DownloadMigrationService {

    MigrationFuture startMigration(NotificationChannelCreator notificationChannelCreator, NotificationCreator<MigrationStatus> notificationCreator);

    interface MigrationFuture {
        void addCallback(@NonNull MigrationCallback migrationCallback);
    }

    interface MigrationCallback {

        @WorkerThread
        void onUpdate(MigrationStatus migrationStatus);

    }

}
