package com.novoda.downloadmanager.lib;

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

import com.novoda.downloadmanager.lib.logger.LLog;
import com.novoda.downloadmanager.notifications.NotificationVisibility;

import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.content.Intent.ACTION_MEDIA_MOUNTED;
import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
import static com.novoda.downloadmanager.lib.Constants.*;
import static com.novoda.downloadmanager.notifications.NotificationVisibility.ACTIVE_OR_COMPLETE;
import static com.novoda.downloadmanager.notifications.NotificationVisibility.ONLY_WHEN_COMPLETE;

/**
 * Receives system broadcasts (boot, network connectivity)
 */
public class DownloadReceiver extends BroadcastReceiver {
    private static final String TAG = "DownloadReceiver";

    static final String EXTRA_BATCH_ID = "com.novoda.downloadmanager.extra.BATCH_ID";

    private static Handler sAsyncHandler;

    static {
        final HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        sAsyncHandler = new Handler(thread.getLooper());
    }

    private final DownloadsUriProvider downloadsUriProvider;
    private DownloadsRepository downloadsRepository;
    private BatchRepository batchRepository;

    public DownloadReceiver() {
        downloadsUriProvider = DownloadsUriProvider.getInstance();
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        RealSystemFacade systemFacade = new RealSystemFacade(context, new Clock());
        ContentResolver contentResolver = context.getContentResolver();
        downloadsRepository = new DownloadsRepository(
                systemFacade,
                contentResolver,
                DownloadsRepository.DownloadInfoCreator.NON_FUNCTIONAL,
                downloadsUriProvider
        );

        batchRepository = new BatchRepository(contentResolver, new DownloadDeleter(contentResolver), downloadsUriProvider, systemFacade);

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
        long[] ids = intent.getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
        int[] statuses = intent.getIntArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_STATUSES);
        switch (action) {
            case ACTION_LIST:
                sendNotificationClickedIntent(context, ids, statuses);
                break;
            case ACTION_OPEN: {
                sendNotificationClickedIntent(context, ids, statuses);
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
                cancelBatchThroughDatabaseState(intent);
                break;
            case ACTION_DELETE:
                deleteDownloadThroughDatabaseState(intent);
                break;
            default:
                // no need to handle any other cases
                break;
        }
    }

    /**
     * Notify the owner of a running download that its notification was clicked.
     */
    private void sendNotificationClickedIntent(Context context, long[] ids, int[] statuses) {
        Intent appIntent = new Intent(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        appIntent.setPackage(context.getPackageName());
        appIntent.putExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS, ids);
        appIntent.putExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_STATUSES, statuses);

        context.sendBroadcast(appIntent);
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
                LLog.w("Missing details for download " + batchId);
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * Mark the given batch as being cancelled by user so it will be cancelled by the running thread.
     */
    private void cancelBatchThroughDatabaseState(Intent intent) {
        long batchId = getBatchId(intent);
        batchRepository.cancelBatch(batchId);
    }

    /**
     * Mark the given {@link DownloadManager#COLUMN_ID} as being deleted by
     * user so it will be deleted by the running thread.
     */
    private void deleteDownloadThroughDatabaseState(Intent intent) {
        Uri downloadUri = getDownloadUri(intent);
        downloadsRepository.deleteDownload(downloadUri);
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
