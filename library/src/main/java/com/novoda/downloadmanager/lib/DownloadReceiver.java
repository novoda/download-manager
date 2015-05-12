package com.novoda.downloadmanager.lib;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
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
import android.provider.BaseColumns;
import android.widget.Toast;

import com.novoda.notils.logger.simple.Log;

import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.content.Intent.ACTION_MEDIA_MOUNTED;
import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
import static com.novoda.downloadmanager.lib.Constants.*;
import static com.novoda.downloadmanager.lib.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;
import static com.novoda.downloadmanager.lib.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION;

/**
 * Receives system broadcasts (boot, network connectivity)
 */
public class DownloadReceiver extends BroadcastReceiver {
    private static final String TAG = "DownloadReceiver";

    public static final String EXTRA_DOWNLOAD_TITLE = "com.novoda.extra.DOWNLOAD_TITLE";
    public static final int TRUE_THIS_IS_CLEARER_NOW = 1;

    private static Handler sAsyncHandler;

    static {
        final HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        sAsyncHandler = new Handler(thread.getLooper());
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String authority = DownloadProvider.determineAuthority(context);
        String action = intent.getAction();
        if (ACTION_BOOT_COMPLETED.equals(action)) {
            startService(context);
        } else if (ACTION_MEDIA_MOUNTED.equals(action)) {
            startService(context);
        } else if (CONNECTIVITY_ACTION.equals(action)) {
            checkConnectivityToStartService(context);
        } else if (ACTION_RETRY.equals(action)) {
            startService(context);
        } else if (ACTION_OPEN.equals(action)
                || ACTION_LIST.equals(action)
                || ACTION_HIDE.equals(action)
                || ACTION_DELETE.equals(action)
                || ACTION_CANCEL.equals(action)) {
            handleSystemNotificationAction(context, authority, intent);
        }
    }

    private void checkConnectivityToStartService(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            startService(context);
        }
    }

    private void handleSystemNotificationAction(final Context context, final String authority, final Intent intent) {
        final PendingResult result = goAsync();
        if (result == null) {
            handleNotificationBroadcast(context, authority, intent);
        } else {
            sAsyncHandler.post(new Runnable() {
                @Override
                public void run() {
                    handleNotificationBroadcast(context, authority, intent);
                    result.finish();
                }
            });
        }
    }

    private void handleNotificationBroadcast(Context context, String authority, Intent intent) {
        String action = intent.getAction();
        if (ACTION_LIST.equals(action)) {
            long[] ids = intent.getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
            sendNotificationClickedIntent(context, authority, ids);
        } else if (ACTION_OPEN.equals(action)) {
            long id = ContentUris.parseId(intent.getData());
            openDownload(context, id);
            hideNotification(context, authority, id);
        } else if (ACTION_HIDE.equals(action)) {
            long id = ContentUris.parseId(intent.getData());
            hideNotification(context, authority, id);
        } else if (ACTION_CANCEL.equals(action)) {
            cancelDownloadThroughDatabaseState(context, authority, intent);
        } else if (ACTION_DELETE.equals(action)) {
            deleteDownloadThroughDatabaseState(context, authority, intent);
        }
    }

    /**
     * Notify the owner of a running download that its notification was clicked.
     */
    private void sendNotificationClickedIntent(Context context, String authority, long[] ids) {
        Uri uri = ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI(authority), ids[0]);

        Intent appIntent = new Intent(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        appIntent.setPackage(context.getPackageName());
        appIntent.putExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS, ids);
        if (ids.length == 1) {
            appIntent.setData(uri);
        } else {
            appIntent.setData(Downloads.Impl.CONTENT_URI(authority));
        }

        context.sendBroadcast(appIntent);
    }

    /**
     * Start activity to display the file represented by the given
     * {@link DownloadManager#COLUMN_ID}.
     */
    private void openDownload(Context context, long id) {
        String authority = DownloadProvider.determineAuthority(context);
        Intent intent = OpenHelper.buildViewIntent(context, authority, id);
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
    private void hideNotification(Context context, String authority, long id) {
        int status;
        int visibility;

        Uri uri = ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI(authority), id);
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                status = getInt(cursor, Downloads.Impl.COLUMN_STATUS);
                visibility = getInt(cursor, Downloads.Impl.COLUMN_VISIBILITY);
            } else {
                Log.w("Missing details for download " + id);
                return;
            }
        } finally {
            cursor.close();
        }

        if (Downloads.Impl.isStatusCompleted(status) && (visibility == VISIBILITY_VISIBLE_NOTIFY_COMPLETED || visibility == VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION)) {
            ContentValues values = new ContentValues();
            values.put(Downloads.Impl.COLUMN_VISIBILITY, Downloads.Impl.VISIBILITY_VISIBLE);
            context.getContentResolver().update(uri, values, null, null);
        }
    }

    /**
     * Mark the given {@link DownloadManager#COLUMN_ID} as being cancelled by
     * user so it will be cancelled by the running thread.
     */
    private void cancelDownloadThroughDatabaseState(Context context, String authority, Intent intent) {
        ContentValues values = new ContentValues(1);
        values.put(Downloads.Impl.COLUMN_STATUS, Downloads.Impl.STATUS_CANCELED);
        context.getContentResolver().update(
                getDownloadUri(context, authority, intent), values, null, null);
    }

    /**
     * Mark the given {@link DownloadManager#COLUMN_ID} as being deleted by
     * user so it will be deleted by the running thread.
     */
    private void deleteDownloadThroughDatabaseState(Context context, String authority, Intent intent) {
        ContentValues values = new ContentValues(1);
        values.put(Downloads.Impl.COLUMN_DELETED, TRUE_THIS_IS_CLEARER_NOW);
        context.getContentResolver().update(
                getDownloadUri(context, authority, intent), values, null, null);
    }

    private Uri getDownloadUri(Context context, String authority, Intent intent) {
        long downloadId = -1;
        if (intent.getData() != null) {
            downloadId = ContentUris.parseId(intent.getData());
        } else if (intent.hasExtra(EXTRA_DOWNLOAD_TITLE)) {
            String title = intent.getStringExtra(EXTRA_DOWNLOAD_TITLE);
            Cursor download = queryDownloads(context, authority, title);
            try {
                if (download.moveToNext()) {
                    downloadId = download.getLong(download.getColumnIndex(BaseColumns._ID));
                } else {
                    Log.e("title to download does not exist as a download");
                }
            } finally {
                download.close();
            }
        }
        return ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI(authority), downloadId);
    }

    private Cursor queryDownloads(Context context, String authority, String title) {
        return context.getContentResolver().query(
                Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI(authority), null,
                Downloads.Impl.COLUMN_TITLE + " = ? AND " +
                    Downloads.Impl.COLUMN_STATUS + " <= ?",
                new String[] { title.toUpperCase(),
                        String.valueOf(Downloads.Impl.STATUS_SUCCESS) }, null);
    }

    private static int getInt(Cursor cursor, String col) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(col));
    }

    private void startService(Context context) {
        context.startService(new Intent(context, DownloadService.class));
    }
}
