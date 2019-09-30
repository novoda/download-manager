package com.novoda.downloadmanager.demo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import com.novoda.downloadmanager.DownloadBatchStatus;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status.ERROR;

public class DownloadBatchStatusView extends ConstraintLayout {

    private TextView statusTextView;

    private DownloadBatchStatusListener downloadBatchStatusListener = DownloadBatchStatusListener.NO_OP;

    public DownloadBatchStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View.inflate(getContext(), R.layout.merge_download_batch_status, this);

        statusTextView = findViewById(R.id.download_batch_status);

        findViewById(R.id.button_pause_downloading).setOnClickListener(view -> downloadBatchStatusListener.onBatchPaused());
        findViewById(R.id.button_resume_downloading).setOnClickListener(view -> downloadBatchStatusListener.onBatchResumed());
    }

    public void setListener(DownloadBatchStatusListener downloadBatchStatusListener) {
        this.downloadBatchStatusListener = downloadBatchStatusListener;
    }

    public void update(DownloadBatchStatus downloadBatchStatus) {
        statusTextView.setText(createMessageFrom(downloadBatchStatus));
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

        void onBatchPaused();

        void onBatchResumed();

        DownloadBatchStatusListener NO_OP = new DownloadBatchStatusListener() {
            @Override
            public void onBatchPaused() {
                // no-op
            }

            @Override
            public void onBatchResumed() {
                // no-op
            }
        };
    }

}
