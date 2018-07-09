package com.novoda.downloadmanager.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.novoda.downloadmanager.AllBatchStatusesCallback;
import com.novoda.downloadmanager.Batch;
import com.novoda.downloadmanager.ConnectionType;
import com.novoda.downloadmanager.DownloadBatchId;
import com.novoda.downloadmanager.DownloadBatchIdCreator;
import com.novoda.downloadmanager.DownloadBatchStatus;
import com.novoda.downloadmanager.DownloadBatchStatusCallback;
import com.novoda.downloadmanager.DownloadFileId;
import com.novoda.downloadmanager.DownloadFileIdCreator;
import com.novoda.downloadmanager.LiteDownloadManagerCommands;
import com.novoda.downloadmanager.StorageRoot;
import com.novoda.downloadmanager.StorageRootFactory;

import java.io.File;

// Need to extract collaborators for this demo to reduce complexity. GH Issue #286
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity"})
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final DownloadBatchId BATCH_ID_1 = DownloadBatchIdCreator.createSanitizedFrom("batch_id_1");
    private static final DownloadBatchId BATCH_ID_2 = DownloadBatchIdCreator.createSanitizedFrom("batch_id_2");
    private static final DownloadFileId FILE_ID_1 = DownloadFileIdCreator.createFrom("file_id_1");
    private static final String FIVE_MB_FILE_URL = "http://ipv4.download.thinkbroadband.com/5MB.zip";
    private static final String TEN_MB_FILE_URL = "http://ipv4.download.thinkbroadband.com/10MB.zip";
    private static final String TWENTY_MB_FILE_URL = "http://ipv4.download.thinkbroadband.com/20MB.zip";

    private DownloadBatchStatusView downloadBatchStatusViewOne;
    private DownloadBatchStatusView downloadBatchStatusViewTwo;

    private LiteDownloadManagerCommands liteDownloadManagerCommands;
    private StorageRoot primaryStorageWithDownloadsSubpackage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DemoApplication demoApplication = (DemoApplication) getApplicationContext();
        liteDownloadManagerCommands = demoApplication.getLiteDownloadManagerCommands();
        liteDownloadManagerCommands.addDownloadBatchCallback(callback);
        liteDownloadManagerCommands.getAllDownloadBatchStatuses(batchStatusesCallback);

        downloadBatchStatusViewOne = findViewById(R.id.batch_1);
        downloadBatchStatusViewTwo = findViewById(R.id.batch_2);

        downloadBatchStatusViewOne.setListener(new DownloadBatchStatusView.DownloadBatchStatusListener() {
            @Override
            public void onBatchPaused() {
                liteDownloadManagerCommands.pause(BATCH_ID_1);
            }

            @Override
            public void onBatchResumed() {
                liteDownloadManagerCommands.resume(BATCH_ID_1);
            }
        });

        downloadBatchStatusViewTwo.setListener(new DownloadBatchStatusView.DownloadBatchStatusListener() {
            @Override
            public void onBatchPaused() {
                liteDownloadManagerCommands.pause(BATCH_ID_2);
            }

            @Override
            public void onBatchResumed() {
                liteDownloadManagerCommands.resume(BATCH_ID_2);
            }
        });

        primaryStorageWithDownloadsSubpackage = StorageRootFactory.createPrimaryStorageDownloadsDirectoryRoot(getApplicationContext());

        CheckBox checkWifiOnly = findViewById(R.id.check_wifi_only);
        checkWifiOnly.setOnCheckedChangeListener(wifiOnlyOnCheckedChange);

        View buttonDownload = findViewById(R.id.button_start_downloading);
        buttonDownload.setOnClickListener(downloadBatchesOnClick);

        View buttonDeleteAll = findViewById(R.id.button_delete_all);
        buttonDeleteAll.setOnClickListener(deleteAllOnClick);

        View buttonLogFileDirectory = findViewById(R.id.button_log_file_directory);
        buttonLogFileDirectory.setOnClickListener(logFileDirectoryOnClick);
    }

    private final CompoundButton.OnCheckedChangeListener wifiOnlyOnCheckedChange = (buttonView, isChecked) -> {
        LiteDownloadManagerCommands downloadManagerCommands = ((DemoApplication) getApplication()).getLiteDownloadManagerCommands();
        if (isChecked) {
            downloadManagerCommands.updateAllowedConnectionType(ConnectionType.UNMETERED);
        } else {
            downloadManagerCommands.updateAllowedConnectionType(ConnectionType.ALL);
        }
    };

    private final View.OnClickListener downloadBatchesOnClick = v -> {
        Batch batch = Batch.with(primaryStorageWithDownloadsSubpackage, BATCH_ID_1, "Made in chelsea")
                .downloadFrom(FIVE_MB_FILE_URL).saveTo("foo/bar", "5mb.zip").withIdentifier(FILE_ID_1).apply()
                .downloadFrom(TEN_MB_FILE_URL).apply()
                .build();
        liteDownloadManagerCommands.download(batch);

        batch = Batch.with(primaryStorageWithDownloadsSubpackage, BATCH_ID_2, "Hollyoaks")
                .downloadFrom(TEN_MB_FILE_URL).apply()
                .downloadFrom(TWENTY_MB_FILE_URL).apply()
                .build();
        liteDownloadManagerCommands.download(batch);
    };

    private final View.OnClickListener deleteAllOnClick = v -> {
        liteDownloadManagerCommands.delete(BATCH_ID_1);
        liteDownloadManagerCommands.delete(BATCH_ID_2);
    };

    private final View.OnClickListener logFileDirectoryOnClick = v -> {
        File downloadsDir = new File(primaryStorageWithDownloadsSubpackage.path());
        Log.d(TAG, "LogFileDirectory. Downloads dir: " + downloadsDir.getAbsolutePath());
        if (downloadsDir.exists()) {
            logAllFiles(downloadsDir.listFiles());
        }
    };

    private void logAllFiles(File... files) {
        for (File file : files) {
            if (file.isDirectory()) {
                logAllFiles(file.listFiles());
            } else {
                Log.d(TAG, "LogFileDirectory. " + file.getAbsolutePath());
            }
        }
    }

    private final DownloadBatchStatusCallback callback = downloadBatchStatus -> {
        DownloadBatchId downloadBatchId = downloadBatchStatus.getDownloadBatchId();
        if (downloadBatchId.equals(BATCH_ID_1)) {
            downloadBatchStatusViewOne.update(downloadBatchStatus);
        } else if (downloadBatchId.equals(BATCH_ID_2)) {
            downloadBatchStatusViewTwo.update(downloadBatchStatus);
        }
    };

    private final AllBatchStatusesCallback batchStatusesCallback = downloadBatchStatuses -> {
        for (DownloadBatchStatus downloadBatchStatus : downloadBatchStatuses) {
            callback.onUpdate(downloadBatchStatus);
        }
    };

}
