package com.novoda.downloadmanager.demo;

import android.app.Notification;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.novoda.downloadmanager.AllBatchStatusesCallback;
import com.novoda.downloadmanager.Batch;
import com.novoda.downloadmanager.DownloadBatchCallback;
import com.novoda.downloadmanager.DownloadBatchId;
import com.novoda.downloadmanager.DownloadBatchIdCreator;
import com.novoda.downloadmanager.DownloadBatchStatus;
import com.novoda.downloadmanager.DownloadMigrator;
import com.novoda.downloadmanager.DownloadMigratorBuilder;
import com.novoda.downloadmanager.LiteDownloadManagerCommands;
import com.novoda.downloadmanager.LocalFilesDirectory;
import com.novoda.downloadmanager.LocalFilesDirectoryFactory;
import com.novoda.downloadmanager.MigrationCallback;
import com.novoda.downloadmanager.MigrationStatus;
import com.novoda.downloadmanager.NotificationConfig;
import com.novoda.downloadmanager.NotificationCustomizer;
import com.novoda.notils.logger.simple.Log;

import java.util.List;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status.ERROR;

public class MainActivity extends AppCompatActivity {

    private static final DownloadBatchId BATCH_ID_1 = DownloadBatchIdCreator.createFrom("batch_id_1");
    private static final DownloadBatchId BATCH_ID_2 = DownloadBatchIdCreator.createFrom("batch_id_2");
    private TextView databaseCloningUpdates;
    private TextView databaseMigrationUpdates;
    private TextView textViewBatch1;
    private TextView textViewBatch2;
    private LiteDownloadManagerCommands liteDownloadManagerCommands;
    private DownloadMigrator downloadMigrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.setShowLogs(true);

        NotificationCustomizer<MigrationStatus> notificationCustomizer = new NotificationCustomizer<MigrationStatus>() {
            @Override
            public Notification customNotificationFrom(NotificationCompat.Builder builder, MigrationStatus payload) {
                return builder
                        .setProgress(payload.totalNumberOfBatchesToMigrate(), payload.numberOfMigratedBatches(), false)
                        .setSmallIcon(android.R.drawable.ic_lock_power_off)
                        .setContentTitle(payload.status().toRawValue())
                        .setContentText(payload.percentageMigrated() + "%")
                        .build();
            }
        };

        NotificationConfig<MigrationStatus> notificationConfig = new NotificationConfig<>(
                this,
                "chocolate",
                "Migration notifications",
                notificationCustomizer,
                NotificationManagerCompat.IMPORTANCE_DEFAULT
        );

        downloadMigrator = DownloadMigratorBuilder.newInstance(this)
                .withNotificationConfig(notificationConfig)
                .build();

        textViewBatch1 = findViewById(R.id.batch_1);
        textViewBatch2 = findViewById(R.id.batch_2);

        final VersionOneDatabaseCloner versionOneDatabaseCloner = DatabaseClonerFactory.databaseCloner(this, cloneCallback);

        final Spinner spinner = findViewById(R.id.database_download_file_size);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.file_sizes, android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);

        databaseCloningUpdates = findViewById(R.id.database_cloning_updates);
        View buttonCreateDB = findViewById(R.id.button_create_v1_db);
        buttonCreateDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedFileSize = (String) spinner.getSelectedItem();
                versionOneDatabaseCloner.cloneDatabaseWithDownloadSize(selectedFileSize);
            }
        });

        databaseMigrationUpdates = findViewById(R.id.database_migration_updates);
        View buttonMigrate = findViewById(R.id.button_migrate);
        buttonMigrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadMigrator.startMigration(migrationCallback);
            }
        });

        View buttonDownload = findViewById(R.id.button_start_downloading);
        buttonDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Batch batch = new Batch.Builder(BATCH_ID_1, "Made in chelsea")
                        .addFile("http://ipv4.download.thinkbroadband.com/10MB.zip")
                        .addFile("http://ipv4.download.thinkbroadband.com/10MB.zip")
                        .build();
                liteDownloadManagerCommands.download(batch);

                batch = new Batch.Builder(BATCH_ID_2, "Hollyoaks")
                        .addFile("http://ipv4.download.thinkbroadband.com/10MB.zip")
                        .addFile("http://ipv4.download.thinkbroadband.com/10MB.zip")
                        .build();
                liteDownloadManagerCommands.download(batch);
            }
        });

        View buttonDeleteAll = findViewById(R.id.button_delete_all);
        buttonDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liteDownloadManagerCommands.delete(BATCH_ID_1);
                liteDownloadManagerCommands.delete(BATCH_ID_2);
            }
        });

        View buttonLogFileDirectory = findViewById(R.id.button_log_file_directory);
        buttonLogFileDirectory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalFilesDirectory localFilesDirectory = LocalFilesDirectoryFactory.create(getApplicationContext());
                for (String fileName : localFilesDirectory.contents()) {
                    Log.d("LogFileDirectory", fileName);
                }
            }
        });

        DemoApplication demoApplication = (DemoApplication) getApplicationContext();
        liteDownloadManagerCommands = demoApplication.getLiteDownloadManagerCommands();
        liteDownloadManagerCommands.addDownloadBatchCallback(callback);
        liteDownloadManagerCommands.getAllDownloadBatchStatuses(new AllBatchStatusesCallback() {
            @Override
            public void onReceived(List<DownloadBatchStatus> downloadBatchStatuses) {
                for (DownloadBatchStatus downloadBatchStatus : downloadBatchStatuses) {
                    callback.onUpdate(downloadBatchStatus);
                }
            }
        });

        bindViews();
    }

    private final VersionOneDatabaseCloner.CloneCallback cloneCallback = new VersionOneDatabaseCloner.CloneCallback() {
        @Override
        public void onUpdate(String updateMessage) {
            databaseCloningUpdates.setText(updateMessage);
        }
    };

    private final MigrationCallback migrationCallback = new MigrationCallback() {
        @Override
        public void onUpdate(MigrationStatus migrationStatus) {
            databaseMigrationUpdates.setText(migrationStatus.status().toRawValue());
        }
    };

    private void bindViews() {
        View buttonPauseDownload1 = findViewById(R.id.button_pause_downloading_1);
        setPause(buttonPauseDownload1, BATCH_ID_1);

        View buttonPauseDownload2 = findViewById(R.id.button_pause_downloading_2);
        setPause(buttonPauseDownload2, BATCH_ID_2);

        View buttonResumeDownload1 = findViewById(R.id.button_resume_downloading_1);
        setResume(buttonResumeDownload1, BATCH_ID_1);

        View buttonResumeDownload2 = findViewById(R.id.button_resume_downloading_2);
        setResume(buttonResumeDownload2, BATCH_ID_2);
    }

    private void setPause(View button, final DownloadBatchId downloadBatchId) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liteDownloadManagerCommands.pause(downloadBatchId);
            }
        });
    }

    private void setResume(View button, final DownloadBatchId downloadBatchId) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liteDownloadManagerCommands.resume(downloadBatchId);
            }
        });
    }

    private final DownloadBatchCallback callback = new DownloadBatchCallback() {
        @Override
        public void onUpdate(DownloadBatchStatus downloadBatchStatus) {
            String status = getStatusMessage(downloadBatchStatus);

            String message = "Batch " + downloadBatchStatus.getDownloadBatchTitle().asString()
                    + "\ndownloaded: " + downloadBatchStatus.percentageDownloaded()
                    + "\nbytes: " + downloadBatchStatus.bytesDownloaded()
                    + status
                    + "\n";

            DownloadBatchId downloadBatchId = downloadBatchStatus.getDownloadBatchId();
            if (downloadBatchId.equals(BATCH_ID_1)) {
                textViewBatch1.setText(message);
            } else if (downloadBatchId.equals(BATCH_ID_2)) {
                textViewBatch2.setText(message);
            }
        }

        @NonNull
        private String getStatusMessage(DownloadBatchStatus downloadBatchStatus) {
            if (downloadBatchStatus.status() == ERROR) {
                return "\nstatus: " + downloadBatchStatus.status()
                        + " - " + downloadBatchStatus.getDownloadErrorType();
            } else {
                return "\nstatus: " + downloadBatchStatus.status();
            }
        }
    };

}
