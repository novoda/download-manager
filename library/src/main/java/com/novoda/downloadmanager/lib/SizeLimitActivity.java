package com.novoda.downloadmanager.lib;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.format.Formatter;

import com.novoda.notils.logger.simple.Log;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Activity to show dialogs to the user when a download exceeds a limit on download sizes for
 * mobile networks.  This activity gets started by the background download service when a download's
 * size is discovered to be exceeded one of these thresholds.
 */
public class SizeLimitActivity extends Activity implements DialogInterface.OnCancelListener, DialogInterface.OnClickListener {

    private final Queue<Intent> downloadsToShow = new LinkedList<>();

    private Dialog dialog;
    private Uri currentUri;
    private Intent currentIntent;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent != null) {
            downloadsToShow.add(intent);
            setIntent(null);
            showNextDialog();
        }
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    private void showNextDialog() {
        if (dialog != null) {
            return;
        }

        if (downloadsToShow.isEmpty()) {
            finish();
            return;
        }

        currentIntent = downloadsToShow.poll();
        currentUri = currentIntent.getData();
        Cursor cursor = getContentResolver().query(currentUri, null, null, null, null);
        try {
            if (!cursor.moveToFirst()) {
                Log.e("Empty cursor for URI " + currentUri);
                dialogClosed();
                return;
            }
            showDialog(cursor);
        } finally {
            cursor.close();
        }
    }

    private void showDialog(Cursor cursor) {
        int size = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadContract.Downloads.COLUMN_TOTAL_BYTES));
        String sizeString = Formatter.formatFileSize(this, size);
        String queueText = "Queue";//getString(R.string.button_queue_for_wifi);
        boolean isWifiRequired = currentIntent.getExtras().getBoolean(DownloadThread.EXTRA_IS_WIFI_REQUIRED);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK);
        if (isWifiRequired) {
            builder.setTitle("Download too large for operator network") //R.string.wifi_required_title
                    .setMessage("You must use Wi-Fi to complete this" + sizeString + " download. " +
                            "\n\n Touch " + queueText + "to start this download the next time " +
                            "you're connected to a Wi-Fi network.") //getString(R.string.wifi_required_body, sizeString, queueText)
                    .setPositiveButton("Queue", this) // R.string.button_queue_for_wifi
                    .setNegativeButton("Cancel", this); // R.string.button_cancel_download
        } else {
            builder.setTitle("Queue for download later?") // R.string.wifi_recommended_title
                    .setMessage("Starting this " + sizeString + " download now may shorten your batter life and/or result in " +
                            "excessive usage of your mobile data connection. Which can lead to charges by your mobile operator " +
                            "depending on your data plan." +
                            "\n\n Touch " + queueText + " to start this download the next time you're connected to a Wi-Fi network.") // getString(R.string.wifi_recommended_body, sizeString, queueText)
                    .setPositiveButton("", this) // R.string.button_start_now
                    .setNegativeButton("", this); // R.string.button_queue_for_wifi
        }
        dialog = builder.setOnCancelListener(this).show();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        dialogClosed();
    }

    private void dialogClosed() {
        dialog = null;
        currentUri = null;
        showNextDialog();
    }

    @Override
    public void onClick(@NonNull DialogInterface dialog, int which) {
        boolean isRequired = currentIntent.getExtras().getBoolean(DownloadThread.EXTRA_IS_WIFI_REQUIRED);
        if (isRequired && which == AlertDialog.BUTTON_NEGATIVE) {
            getContentResolver().delete(currentUri, null, null);
        } else if (!isRequired && which == AlertDialog.BUTTON_POSITIVE) {
            ContentValues values = new ContentValues();
            values.put(DownloadContract.Downloads.COLUMN_BYPASS_RECOMMENDED_SIZE_LIMIT, true);
            getContentResolver().update(currentUri, values, null, null);
        }
        dialogClosed();
    }
}
