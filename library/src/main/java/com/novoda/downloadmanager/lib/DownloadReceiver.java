package com.novoda.downloadmanager.lib;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.novoda.notils.logger.simple.Log;

import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.content.Intent.ACTION_MEDIA_MOUNTED;
import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
import static com.novoda.downloadmanager.lib.Constants.*;
import static com.novoda.downloadmanager.lib.NotificationVisibility.ACTIVE_OR_COMPLETE;
import static com.novoda.downloadmanager.lib.NotificationVisibility.ONLY_WHEN_COMPLETE;

/**
 * Receives system broadcasts (boot, network connectivity)
 */
public class DownloadReceiver extends BroadcastReceiver {
    private static final String TAG = "DownloadReceiver";

    static final String EXTRA_BATCH_ID = "com.novoda.extra.BATCH_ID";
    private static final int TRUE_THIS_IS_CLEARER_NOW = 1;

    private static Handler sAsyncHandler;

    static {
        final HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        sAsyncHandler = new Handler(thread.getLooper());
    }

    private final DownloadsUriProvider downloadsUriProvider;

    public DownloadReceiver() {
        downloadsUriProvider = DownloadsUriProvider.getInstance();
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        switch (intent.getAction()) {
            case ACTION_BOOT_COMPLETED:
            case ACTION_MEDIA_MOUNTED:
            case ACTION_RETRY:
                startService(context);
                break;
            case CONNECTIVITY_ACTION:
                checkConnectivityToStartService(context);
                break;
            case ACTION_OPEN:
            case ACTION_LIST:
            case ACTION_HIDE:
            case ACTION_DELETE:
            case ACTION_CANCEL:
                handleSystemNotificationAction(context, intent);
                break;
            default:
                // no need to handle any other cases
                break;
        }
    }

    private void checkConnectivityToStartService(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            startService(context);
        }
    }

    private void handleSystemNotificationAction(final Context context, final Intent intent) {
        final PendingResult result = goAsync();
        if (result == null) {
            handleNotificationBroadcast(context, intent);
        } else {
            sAsyncHandler.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            handleNotificationBroadcast(context, intent);
                            result.finish();
                        }
                    });
        }
    }

    private void handleNotificationBroadcast(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case ACTION_LIST:
                long[] ids = intent.getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
                sendNotificationClickedIntent(context, ids);
                break;
            case ACTION_OPEN: {
                long id = ContentUris.parseId(intent.getData());
                openDownload(context, id);
                long batchId = getBatchId(intent);
                hideNotification(context, batchId);
                break;
            }
            case ACTION_HIDE: {
                long batchId = getBatchId(intent);
                hideNotification(context, batchId);
                break;
            }
            case ACTION_CANCEL:
                cancelBatchThroughDatabaseState(context, intent);
                break;
            case ACTION_DELETE:
                deleteDownloadThroughDatabaseState(context, intent);
                break;
            default:
                // no need to handle any other cases
                break;
        }
    }

    /**
     * Notify the owner of a running download that its notification was clicked.
     */
    private void sendNotificationClickedIntent(Context context, long[] ids) {
        Uri uri = ContentUris.withAppendedId(downloadsUriProvider.getAllDownloadsUri(), ids[0]);

        Intent appIntent = new Intent(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        appIntent.setPackage(context.getPackageName());
        appIntent.putExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS, ids);
        if (ids.length == 1) {
            appIntent.setData(uri);
        } else {
            appIntent.setData(downloadsUriProvider.getContentUri());
        }

        context.sendBroadcast(appIntent);
    }

    /**
     * Start activity to display the file represented by the given
     * {@link DownloadManager#COLUMN_ID}.
     */
    private void openDownload(Context context, long id) {
        ContentResolver contentResolver = context.getContentResolver();
        DownloadManager downloadManager = DownloadManager.newInstance(context, contentResolver);
        OpenHelper openHelper = new OpenHelper(downloadManager, this.downloadsUriProvider);
        Intent intent = openHelper.buildViewIntent(context, id);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Log.d("no activity for " + intent, ex);
            Toast.makeText(context, "Cannot open file", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Mark the given {@link DownloadManager#COLUMN_ID} as being acknowledged by
     * user so it's not renewed later.
     */
    private void hideNotification(Context context, long batchId) {
        Uri uri = ContentUris.withAppendedId(downloadsUriProvider.getBatchesUri(), batchId);
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                int status = getInt(cursor, DownloadContract.Batches.COLUMN_STATUS);
                @NotificationVisibility.Value int visibility = getInt(cursor, DownloadContract.Batches.COLUMN_VISIBILITY);

                if ((DownloadStatus.isCancelled(status) || DownloadStatus.isCompleted(status))
                        && (visibility == ACTIVE_OR_COMPLETE || visibility == ONLY_WHEN_COMPLETE)) {
                    ContentValues values = new ContentValues(1);
                    values.put(DownloadContract.Batches.COLUMN_VISIBILITY, NotificationVisibility.ONLY_WHEN_ACTIVE);
                    context.getContentResolver().update(uri, values, null, null);
                }
            } else {
                Log.w("Missing details for download " + batchId);
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * Mark the given batch as being cancelled by user so it will be cancelled by the running thread.
     */
    private void cancelBatchThroughDatabaseState(Context context, Intent intent) {
        ContentValues downloadValues = new ContentValues(1);
        downloadValues.put(DownloadContract.Downloads.COLUMN_STATUS, DownloadStatus.CANCELED);
        long batchId = getBatchId(intent);
        context.getContentResolver().update(
                downloadsUriProvider.getAllDownloadsUri(),
                downloadValues,
                DownloadContract.Downloads.COLUMN_BATCH_ID + " = ?",
                new String[]{String.valueOf(batchId)}
        );
        ContentValues batchValues = new ContentValues(1);
        batchValues.put(DownloadContract.Batches.COLUMN_STATUS, DownloadStatus.CANCELED);
        context.getContentResolver().update(
                ContentUris.withAppendedId(downloadsUriProvider.getBatchesUri(), batchId),
                batchValues,
                null,
                null
        );
    }

    /**
     * Mark the given {@link DownloadManager#COLUMN_ID} as being deleted by
     * user so it will be deleted by the running thread.
     */
    private void deleteDownloadThroughDatabaseState(Context context, Intent intent) {
        ContentValues values = new ContentValues(1);
        values.put(DownloadContract.Downloads.COLUMN_DELETED, TRUE_THIS_IS_CLEARER_NOW);
        context.getContentResolver().update(getDownloadUri(intent), values, null, null);
    }

    private Uri getDownloadUri(Intent intent) {
        long downloadId = -1;
        if (intent.getData() != null) {
            downloadId = ContentUris.parseId(intent.getData());
        }
        return ContentUris.withAppendedId(downloadsUriProvider.getAllDownloadsUri(), downloadId);
    }

    private long getBatchId(Intent intent) {
        return intent.getLongExtra(EXTRA_BATCH_ID, -1);
    }

    private static int getInt(Cursor cursor, String col) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(col));
    }

    private void startService(Context context) {
        context.startService(new Intent(context, DownloadService.class));
    }
}
