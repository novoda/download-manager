package com.novoda.downloadmanager.demo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.novoda.downloadmanager.DownloadManager;
import com.novoda.downloadmanager.StorageRootFactory;
import com.novoda.downloadmanager.demo.migration.MigrationJob;

import java.util.concurrent.Executors;

public class MigrationActivity extends AppCompatActivity {

    private VersionOneDatabaseCloner versionOneDatabaseCloner;
    private MigrationJob migrationJob;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_migration);

        DemoApplication demoApplication = (DemoApplication) getApplicationContext();
        DownloadManager downloadManager = demoApplication.getDownloadManager();

        TextView databaseMigrationUpdates = findViewById(R.id.database_migration_updates);
        Handler migrationCallbackHandler = new Handler(Looper.getMainLooper());
        migrationJob = new MigrationJob(
                getDatabasePath("downloads.db"),
                StorageRootFactory.createPrimaryStorageDownloadsDirectoryRoot(getApplicationContext()),
                new PrimaryStoragePicturesDirectoryRoot(getApplicationContext()),
                downloadManager,
                migrationCallbackHandler,
                databaseMigrationUpdates::setText
        );
        findViewById(R.id.button_migrate).setOnClickListener(view -> Executors.newSingleThreadExecutor().submit(migrationJob));

        TextView databaseCloningUpdates = findViewById(R.id.database_cloning_updates);
        versionOneDatabaseCloner = DatabaseClonerFactory.databaseCloner(this, databaseCloningUpdates::setText);

        Spinner downloadFileSizeSpinner = findViewById(R.id.database_download_file_size);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.file_sizes, android.R.layout.simple_spinner_item);
        downloadFileSizeSpinner.setAdapter(adapter);
        findViewById(R.id.button_create_v1_db).setOnClickListener(view -> {
            String selectedFileSize = (String) downloadFileSizeSpinner.getSelectedItem();
            versionOneDatabaseCloner.cloneDatabaseWithDownloadSize(selectedFileSize);
        });
    }
}
