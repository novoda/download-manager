package com.novoda.downloadmanager;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

public interface DownloadMigrationService {

    MigrationFuture startMigration(NotificationChannelCreator notificationChannelCreator, NotificationCreator<MigrationStatus> notificationCreator);

    interface MigrationFuture {
        void observe(@NonNull MigrationCallback migrationCallback);
    }

    interface MigrationCallback {

        @WorkerThread
        void onUpdate(MigrationStatus migrationStatus);

        MigrationCallback NO_OP = new MigrationCallback() {
            @Override
            public void onUpdate(MigrationStatus migrationStatus) {
                // do nothing.
            }
        };
    }

}
