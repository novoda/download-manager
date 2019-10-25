package com.novoda.downloadmanager.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.novoda.downloadmanager.AllBatchStatusesCallback;
import com.novoda.downloadmanager.Batch;
import com.novoda.downloadmanager.ConnectionType;
import com.novoda.downloadmanager.DownloadBatchId;
import com.novoda.downloadmanager.DownloadBatchIdCreator;
import com.novoda.downloadmanager.DownloadBatchStatus;
import com.novoda.downloadmanager.DownloadBatchStatusCallback;
import com.novoda.downloadmanager.DownloadFileId;
import com.novoda.downloadmanager.DownloadFileIdCreator;
import com.novoda.downloadmanager.DownloadManager;
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
    private static final Long MAX_BATCH_SIZE = 60L * 1024L * 1024L; // 60 MB

    private DownloadBatchStatusView downloadBatchStatusViewOne;
    private DownloadBatchStatusView downloadBatchStatusViewTwo;

    private DownloadManager downloadManager;
    private StorageRoot primaryStorageWithDownloadsSubpackage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DemoApplication demoApplication = (DemoApplication) getApplicationContext();
        downloadManager = demoApplication.getDownloadManager();
        downloadManager.addDownloadBatchCallback(callback);
        downloadManager.getAllDownloadBatchStatuses(batchStatusesCallback);
        DemoBatchSizeProvider batchSizeProvider = demoApplication.getBatchSizeProvider();
        batchSizeProvider.setMaxSizeOfBatch(MAX_BATCH_SIZE);

        downloadBatchStatusViewOne = findViewById(R.id.batch_1);
        downloadBatchStatusViewTwo = findViewById(R.id.batch_2);

        downloadBatchStatusViewOne.setListener(new DownloadBatchStatusView.DownloadBatchStatusListener() {
            @Override
            public void onBatchPaused() {
                downloadManager.pause(BATCH_ID_1);
            }

            @Override
            public void onBatchResumed() {
                downloadManager.resume(BATCH_ID_1);
            }
        });

        downloadBatchStatusViewTwo.setListener(new DownloadBatchStatusView.DownloadBatchStatusListener() {
            @Override
            public void onBatchPaused() {
                downloadManager.pause(BATCH_ID_2);
            }

            @Override
            public void onBatchResumed() {
                downloadManager.resume(BATCH_ID_2);
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

        TextView batchSizeLabel = findViewById(R.id.storage_size_label);
        batchSizeLabel.setText(getString(R.string.max_batch_size, MAX_BATCH_SIZE));

        SeekBar batchSizeSeekBar = findViewById(R.id.batch_size_seek_bar);
        batchSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                long maxSizeOfBatch = MAX_BATCH_SIZE * progress / batchSizeSeekBar.getMax();
                batchSizeLabel.setText(getString(R.string.max_batch_size, maxSizeOfBatch));
                batchSizeProvider.setMaxSizeOfBatch(maxSizeOfBatch);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "SeekBar#onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "SeekBar#onStopTrackingTouch");
            }
        });
    }

    private final CompoundButton.OnCheckedChangeListener wifiOnlyOnCheckedChange = (buttonView, isChecked) -> {
        DownloadManager downloadManagerCommands = ((DemoApplication) getApplication()).getDownloadManager();
        if (isChecked) {
            downloadManagerCommands.updateAllowedConnectionType(ConnectionType.UNMETERED);
        } else {
            downloadManagerCommands.updateAllowedConnectionType(ConnectionType.ALL);
        }
    };

    private final View.OnClickListener downloadBatchesOnClick = v -> {
        Batch batch = Batch.with(primaryStorageWithDownloadsSubpackage, BATCH_ID_1, "Batch 1 Title")
                .downloadFrom(FIVE_MB_FILE_URL).saveTo("foo/bar", "5mb.zip").withIdentifier(FILE_ID_1).apply()
                .downloadFrom(TEN_MB_FILE_URL).apply()
                .build();
        downloadManager.download(batch);

        batch = Batch.with(primaryStorageWithDownloadsSubpackage, BATCH_ID_2, "Batch 2 Title")
                .downloadFrom(TEN_MB_FILE_URL).apply()
                .downloadFrom(TWENTY_MB_FILE_URL).apply()
                .build();
        downloadManager.download(batch);
    };

    private final View.OnClickListener deleteAllOnClick = v -> {
        downloadManager.delete(BATCH_ID_1);
        downloadManager.delete(BATCH_ID_2);
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
