package com.novoda.downloadmanager.demo;

import android.app.Notification;
import android.os.Bundle;
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
import com.novoda.downloadmanager.DownloadFileId;
import com.novoda.downloadmanager.DownloadFileIdCreator;
import com.novoda.downloadmanager.DownloadMigrator;
import com.novoda.downloadmanager.DownloadMigratorBuilder;
import com.novoda.downloadmanager.LiteDownloadManagerCommands;
import com.novoda.downloadmanager.LocalFilesDirectory;
import com.novoda.downloadmanager.LocalFilesDirectoryFactory;
import com.novoda.downloadmanager.MigrationCallback;
import com.novoda.downloadmanager.MigrationStatus;
import com.novoda.downloadmanager.NotificationCreator;
import com.novoda.downloadmanager.NotificationCustomizer;
import com.novoda.notils.logger.simple.Log;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status.ERROR;

// Need to extract collaborators for this demo to reduce complexity. GH Issue #286
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity"})
public class MainActivity extends AppCompatActivity {

    private static final DownloadBatchId BATCH_ID_1 = DownloadBatchIdCreator.createFrom("batch_id_1");
    private static final DownloadBatchId BATCH_ID_2 = DownloadBatchIdCreator.createFrom("batch_id_2");
    private static final DownloadFileId FILE_ID_1 = DownloadFileIdCreator.createFrom("file_id_1");
    private static final DownloadFileId FILE_ID_2 = DownloadFileIdCreator.createFrom("file_id_2");
    private static final String FIVE_MB_FILE_URL = "http://ipv4.download.thinkbroadband.com/5MB.zip";
    private static final String TEN_MB_FILE_URL = "http://ipv4.download.thinkbroadband.com/10MB.zip";
    private static final String TWENTY_FILE_URL = "http://ipv4.download.thinkbroadband.com/20MB.zip";

    private TextView databaseCloningUpdates;
    private TextView databaseMigrationUpdates;
    private TextView textViewBatch1;
    private TextView textViewBatch2;
    private LiteDownloadManagerCommands liteDownloadManagerCommands;
    private DownloadMigrator downloadMigrator;
    private VersionOneDatabaseCloner versionOneDatabaseCloner;
    private Spinner downloadFileSizeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.setShowLogs(true);

        NotificationCustomizer<MigrationStatus> notificationCustomizer = new MigrationNotificationCustomizer();

        NotificationCreator<MigrationStatus> notificationCreator = new NotificationCreator<>(
                this,
                "chocolate",
                "Migration notifications",
                NotificationManagerCompat.IMPORTANCE_DEFAULT,
                notificationCustomizer
        );

        downloadMigrator = DownloadMigratorBuilder.newInstance(this)
                .withNotification(notificationCreator)
                .build();

        textViewBatch1 = findViewById(R.id.batch_1);
        textViewBatch2 = findViewById(R.id.batch_2);

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

        View buttonDownload = findViewById(R.id.button_start_downloading);
        buttonDownload.setOnClickListener(downloadBatchesOnClick);

        View buttonDeleteAll = findViewById(R.id.button_delete_all);
        buttonDeleteAll.setOnClickListener(deleteAllOnClick);

        View buttonLogFileDirectory = findViewById(R.id.button_log_file_directory);
        buttonLogFileDirectory.setOnClickListener(logFileDirectoryOnClick);

        View buttonLogDownloadFileStatus = findViewById(R.id.button_log_download_file_status);
        buttonLogDownloadFileStatus.setOnClickListener(logDownloadFileStatusOnClick);

        DemoApplication demoApplication = (DemoApplication) getApplicationContext();
        liteDownloadManagerCommands = demoApplication.getLiteDownloadManagerCommands();
        liteDownloadManagerCommands.addDownloadBatchCallback(callback);
        liteDownloadManagerCommands.getAllDownloadBatchStatuses(batchStatusesCallback);

        bindBatchViews();
    }

    private final VersionOneDatabaseCloner.CloneCallback cloneCallback = updateMessage -> databaseCloningUpdates.setText(updateMessage);

    private final View.OnClickListener createDatabaseOnClick = v -> {
        String selectedFileSize = (String) downloadFileSizeSpinner.getSelectedItem();
        versionOneDatabaseCloner.cloneDatabaseWithDownloadSize(selectedFileSize);
    };

    private final MigrationCallback migrationCallback = migrationStatus -> databaseMigrationUpdates.setText(migrationStatus.status().toRawValue());

    private final View.OnClickListener startMigrationOnClick = v -> downloadMigrator.startMigration(migrationCallback);

    private final View.OnClickListener downloadBatchesOnClick = v -> {
        Batch batch = new Batch.Builder(BATCH_ID_1, "Made in chelsea")
                .addFile(FILE_ID_1, FIVE_MB_FILE_URL)
                .addFile(TEN_MB_FILE_URL)
                .build();
        liteDownloadManagerCommands.download(batch);

        batch = new Batch.Builder(BATCH_ID_2, "Hollyoaks")
                .addFile(FILE_ID_2, TEN_MB_FILE_URL)
                .addFile(TWENTY_FILE_URL)
                .build();
        liteDownloadManagerCommands.download(batch);
    };

    private final View.OnClickListener deleteAllOnClick = v -> {
        liteDownloadManagerCommands.delete(BATCH_ID_1);
        liteDownloadManagerCommands.delete(BATCH_ID_2);
    };

    private final View.OnClickListener logFileDirectoryOnClick = v -> {
        LocalFilesDirectory localFilesDirectory = LocalFilesDirectoryFactory.create(getApplicationContext());
        for (String fileName : localFilesDirectory.contents()) {
            Log.d("LogFileDirectory", fileName);
        }
    };

    private final View.OnClickListener logDownloadFileStatusOnClick = v -> liteDownloadManagerCommands.getDownloadStatusWithMatching(
            FILE_ID_1,
            downloadFileStatus -> Log.d("FileStatus: ", downloadFileStatus)
    );

    private final DownloadBatchCallback callback = downloadBatchStatus -> {
        String status = getStatusMessage(downloadBatchStatus);

        String message = "Batch " + downloadBatchStatus.getDownloadBatchTitle().asString()
                + "\ndownloaded: " + downloadBatchStatus.percentageDownloaded() + "%"
                + "\nbytes: " + downloadBatchStatus.bytesDownloaded()
                + status
                + "\n";

        DownloadBatchId downloadBatchId = downloadBatchStatus.getDownloadBatchId();
        if (downloadBatchId.equals(BATCH_ID_1)) {
            textViewBatch1.setText(message);
        } else if (downloadBatchId.equals(BATCH_ID_2)) {
            textViewBatch2.setText(message);
        }
    };

    private String getStatusMessage(DownloadBatchStatus downloadBatchStatus) {
        if (downloadBatchStatus.status() == ERROR) {
            return "\nstatus: " + downloadBatchStatus.status()
                    + " - " + downloadBatchStatus.getDownloadErrorType();
        } else {
            return "\nstatus: " + downloadBatchStatus.status();
        }
    }

    private final AllBatchStatusesCallback batchStatusesCallback = downloadBatchStatuses -> {
        for (DownloadBatchStatus downloadBatchStatus : downloadBatchStatuses) {
            callback.onUpdate(downloadBatchStatus);
        }
    };

    private void bindBatchViews() {
        View buttonPauseDownload1 = findViewById(R.id.button_pause_downloading_1);
        buttonPauseDownload1.setOnClickListener(v -> liteDownloadManagerCommands.pause(BATCH_ID_1));

        View buttonPauseDownload2 = findViewById(R.id.button_pause_downloading_2);
        buttonPauseDownload2.setOnClickListener(v -> liteDownloadManagerCommands.pause(BATCH_ID_2));

        View buttonResumeDownload1 = findViewById(R.id.button_resume_downloading_1);
        buttonResumeDownload1.setOnClickListener(v -> liteDownloadManagerCommands.resume(BATCH_ID_1));

        View buttonResumeDownload2 = findViewById(R.id.button_resume_downloading_2);
        buttonResumeDownload2.setOnClickListener(v -> liteDownloadManagerCommands.resume(BATCH_ID_2));
    }

    private static class MigrationNotificationCustomizer implements NotificationCustomizer<MigrationStatus> {
        @Override
        public Notification customNotificationFrom(NotificationCompat.Builder builder, MigrationStatus payload) {
            return builder
                    .setProgress(payload.totalNumberOfBatchesToMigrate(), payload.numberOfMigratedBatches(), false)
                    .setSmallIcon(android.R.drawable.ic_lock_power_off)
                    .setContentTitle(payload.status().toRawValue())
                    .setContentText(payload.percentageMigrated() + "%")
                    .build();
        }
    }
}
