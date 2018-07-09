package com.novoda.downloadmanager.demo;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.novoda.downloadmanager.DownloadBatchStatus;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status.ERROR;

public class DownloadBatchStatusView extends ConstraintLayout {

    private TextView downloadBatchStatusView;

    private DownloadBatchStatusListener downloadBatchStatusListener;
    private DownloadBatchStatus downloadBatchStatus;

    public DownloadBatchStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View.inflate(getContext(), R.layout.merge_download_batch_status, this);

        downloadBatchStatusView = findViewById(R.id.download_batch_status);
        View pauseDownloadView = findViewById(R.id.button_pause_downloading);
        View resumeDownloadView = findViewById(R.id.button_resume_downloading);

        pauseDownloadView.setOnClickListener(view -> {
            if (downloadBatchStatusListener == null || downloadBatchStatus == null) {
                return;
            }
            downloadBatchStatusListener.onBatchPaused(downloadBatchStatus);
        });

        resumeDownloadView.setOnClickListener(view -> {
            if (downloadBatchStatusListener == null || downloadBatchStatus == null) {
                return;
            }
            downloadBatchStatusListener.onBatchResumed(downloadBatchStatus);
        });
    }

    public void setListener(DownloadBatchStatusListener downloadBatchStatusListener) {
        this.downloadBatchStatusListener = downloadBatchStatusListener;
    }

    public void update(DownloadBatchStatus downloadBatchStatus) {
        this.downloadBatchStatus = downloadBatchStatus;
        downloadBatchStatusView.setText(createMessageFrom(downloadBatchStatus));
    }

    private String createMessageFrom(DownloadBatchStatus downloadBatchStatus) {
        String status = getStatusMessage(downloadBatchStatus);

        return "Batch " + downloadBatchStatus.getDownloadBatchTitle().asString()
                + "\ndownloaded: " + downloadBatchStatus.percentageDownloaded() + "%"
                + "\nbytes: " + downloadBatchStatus.bytesDownloaded()
                + "\ntotal: " + downloadBatchStatus.bytesTotalSize()
                + status
                + "\n";
    }

    private String getStatusMessage(DownloadBatchStatus downloadBatchStatus) {
        if (downloadBatchStatus.status() == ERROR) {
            return "\nstatus: " + downloadBatchStatus.status()
                    + " - " + downloadBatchStatus.downloadError().type();
        } else {
            return "\nstatus: " + downloadBatchStatus.status();
        }
    }

    interface DownloadBatchStatusListener {

        void onBatchPaused(DownloadBatchStatus downloadBatchStatus);

        void onBatchResumed(DownloadBatchStatus downloadBatchStatus);
    }

}
