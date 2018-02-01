package com.novoda.downloadmanager.demo;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

final class DatabaseClonerFactory {

    private DatabaseClonerFactory() {
        // Uses static factory method.
    }

    static VersionOneDatabaseCloner databaseCloner(Context context, final VersionOneDatabaseCloner.CloneCallback cloneCallback) {
        String originalDatabaseLocation = context.getFilesDir().getParentFile().getAbsolutePath() + "/databases/downloads.db";
        Executor executor = Executors.newSingleThreadExecutor();
        AssetManager assets = context.getAssets();
        final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        VersionOneDatabaseCloner.CloneCallback mainThreadReportingCloneCallback = new VersionOneDatabaseCloner.CloneCallback() {
            @Override
            public void onUpdate(String updateMessage) {
                mainThreadHandler.post(() -> cloneCallback.onUpdate(updateMessage));
            }
        };

        return new VersionOneDatabaseCloner(
                assets,
                executor,
                mainThreadReportingCloneCallback,
                originalDatabaseLocation
        );
    }
}
