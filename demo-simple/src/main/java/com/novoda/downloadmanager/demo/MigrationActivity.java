package com.novoda.downloadmanager.demo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.novoda.downloadmanager.DownloadMigrator;
import com.novoda.downloadmanager.DownloadMigratorBuilder;
import com.novoda.downloadmanager.LiteDownloadManagerCommands;
import com.novoda.downloadmanager.MigrationCallback;
import com.novoda.downloadmanager.MigrationStatus;

public class MigrationActivity extends AppCompatActivity {

    private static final String TAG = MigrationActivity.class.getSimpleName();
    @SuppressLint("SdCardPath")
    private static final String V1_BASE_PATH = "/data/data/com.novoda.downloadmanager.demo.simple/files/Pictures/";

    private TextView databaseCloningUpdates;
    private TextView databaseMigrationUpdates;
    private Spinner downloadFileSizeSpinner;

    private VersionOneDatabaseCloner versionOneDatabaseCloner;
    private DownloadMigrator downloadMigrator;
    private LiteDownloadManagerCommands liteDownloadManagerCommands;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_migration);

        downloadMigrator = DownloadMigratorBuilder.newInstance(this, R.mipmap.ic_launcher_round)
                .withNotificationChannel(
                        "chocolate",
                        "Migration notifications",
                        NotificationManagerCompat.IMPORTANCE_DEFAULT
                )
                .withMigrationCallback(migrationCallback)
                .build();

        versionOneDatabaseCloner = DatabaseClonerFactory.databaseCloner(this, cloneCallback);

        downloadFileSizeSpinner = findViewById(R.id.database_download_file_size);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.file_sizes, android.R.layout.simple_spinner_item);
        downloadFileSizeSpinner.setAdapter(adapter);

        databaseCloningUpdates = findViewById(R.id.database_cloning_updates);
        View buttonCreateDB = findViewById(R.id.button_create_v1_db);
        buttonCreateDB.setOnClickListener(createDatabaseOnClick);

        databaseMigrationUpdates = findViewById(R.id.database_migration_updates);
        View buttonMigrate = findViewById(R.id.button_migrate);
        buttonMigrate.setOnClickListener(startMigrationOnClick);

        DemoApplication demoApplication = (DemoApplication) getApplicationContext();
        liteDownloadManagerCommands = demoApplication.getLiteDownloadManagerCommands();
    }

    private final VersionOneDatabaseCloner.CloneCallback cloneCallback = updateMessage -> databaseCloningUpdates.setText(updateMessage);

    private final View.OnClickListener createDatabaseOnClick = v -> {
        String selectedFileSize = (String) downloadFileSizeSpinner.getSelectedItem();
        versionOneDatabaseCloner.cloneDatabaseWithDownloadSize(selectedFileSize);
    };

    private final MigrationCallback migrationCallback = migrationStatus -> {
        if (migrationStatus.status() == MigrationStatus.Status.COMPLETE) {
            liteDownloadManagerCommands.submitAllStoredDownloads(() -> Log.d(TAG, "Migration completed, submitting all downloads"));
        }
        databaseMigrationUpdates.setText(migrationStatus.status().toRawValue());
    };

    private final View.OnClickListener startMigrationOnClick = v -> {
        downloadMigrator.startMigration("migrationJob", getDatabasePath("downloads.db"), V1_BASE_PATH);
    };
}
