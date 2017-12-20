package com.novoda.downloadmanager;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

class MigrationFutureWithCallbacks implements DownloadMigrationService.MigrationFuture {

    private final List<DownloadMigrationService.MigrationCallback> callbacks = new ArrayList<>();

    public void addCallback(@NonNull DownloadMigrationService.MigrationCallback callback) {
        callbacks.add(callback);
    }

    protected void onUpdate(MigrationStatus status) {
        for (DownloadMigrationService.MigrationCallback callback : callbacks) {
            callback.onUpdate(status);
        }
    }
}
