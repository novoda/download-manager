package com.novoda.downloadmanager.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.novoda.downloadmanager.Download;
import com.novoda.downloadmanager.R;
import com.novoda.downloadmanager.lib.DownloadBatch;
import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.lib.DownloadReceiver;
import com.novoda.downloadmanager.lib.DownloadStatus;
import com.novoda.downloadmanager.lib.PublicFacingDownloadMarshaller;
import com.novoda.downloadmanager.lib.PublicFacingStatusTranslator;
import com.novoda.downloadmanager.notifications.DownloadNotifier.NotificationCreatedCallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NotificationDisplayer {

    public static final String ACTION_NOTIFICATION_DISMISSED = "com.novoda.downloadmanager.action.NOTIFICATION_DISMISSED";
    public static final String ACTION_DOWNLOAD_FAILED_CLICK = "com.novoda.downloadmanager.action.DOWNLOAD_FAILED_CLICK";
    public static final String ACTION_DOWNLOAD_RUNNING_CLICK = "com.novoda.downloadmanager.action.DOWNLOAD_RUNNING_CLICK";
    public static final String ACTION_DOWNLOAD_SUCCESS_CLICK = "com.novoda.downloadmanager.action.DOWNLOAD_SUCCESS_CLICK";
    public static final String ACTION_DOWNLOAD_CANCELLED_CLICK = "com.novoda.downloadmanager.action.DOWNLOAD_CANCELLED_CLICK";
    public static final String ACTION_DOWNLOAD_SUBMITTED_CLICK = "com.novoda.downloadmanager.action.DOWNLOAD_SUBMITTED_CLICK";
    public static final String ACTION_DOWNLOAD_OTHER_CLICK = "com.novoda.downloadmanager.action.DOWNLOAD_OTHER_CLICK";

    private final Context context;
    private final NotificationManager notificationManager;
    private final NotificationImageRetriever imageRetriever;
    private final Resources resources;
    /**
     * Current speed of active downloads, mapped from {@link DownloadBatch#batchId}
     * to speed in bytes per second.
     */
    private final LongSparseArray<Long> downloadSpeed = new LongSparseArray<>();
    private final NotificationCustomiser notificationCustomiser;
    private final PublicFacingStatusTranslator statusTranslator;
    private final PublicFacingDownloadMarshaller downloadMarshaller;

    public NotificationDisplayer(
            Context context,
            NotificationManager notificationManager,
            NotificationImageRetriever imageRetriever,
            Resources resources,
            NotificationCustomiser notificationCustomiser,
            PublicFacingStatusTranslator statusTranslator,
            PublicFacingDownloadMarshaller downloadMarshaller) {
        this.context = context;
        this.notificationManager = notificationManager;
        this.imageRetriever = imageRetriever;
        this.resources = resources;
        this.notificationCustomiser = notificationCustomiser;
        this.statusTranslator = statusTranslator;
        this.downloadMarshaller = downloadMarshaller;
    }

    public void buildAndShowNotification(NotificationTag tag, Collection<DownloadBatch> batchesForTag, long firstShown, NotificationCreatedCallback callback) {
        int type = tag.status();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setWhen(firstShown);
        buildIcon(type, builder);
        buildActionIntents(type, batchesForTag, builder);

        Notification notification = buildTitlesAndDescription(type, batchesForTag, builder);
        notificationManager.notify(tag.hashCode(), notification);
        callback.onNotificationCreated(tag.hashCode(), notification);
    }

    private void buildIcon(int type, NotificationCompat.Builder builder) {
        switch (type) {
            case SynchronisedDownloadNotifier.TYPE_ACTIVE:
                builder.setSmallIcon(android.R.drawable.stat_sys_download);
                break;
            case SynchronisedDownloadNotifier.TYPE_WAITING:
            case SynchronisedDownloadNotifier.TYPE_FAILED:
                builder.setSmallIcon(android.R.drawable.stat_sys_warning);
                break;
            case SynchronisedDownloadNotifier.TYPE_SUCCESS:
                builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
                break;
            default:
                builder.setSmallIcon(android.R.drawable.stat_sys_warning);
                break;
        }
    }

    private void buildActionIntents(int type, Collection<DownloadBatch> cluster, NotificationCompat.Builder builder) {
        DownloadBatch batch = cluster.iterator().next();
        long batchId = batch.getBatchId();
        int batchStatus = batch.getStatus();

        if (type == SynchronisedDownloadNotifier.TYPE_ACTIVE || type == SynchronisedDownloadNotifier.TYPE_WAITING) {
            builder.setOngoing(true);
        } else if (type == SynchronisedDownloadNotifier.TYPE_SUCCESS
                || type == SynchronisedDownloadNotifier.TYPE_CANCELLED
                || type == SynchronisedDownloadNotifier.TYPE_FAILED) {
            Intent dismissedIntent = createNotificationDismissedIntent(batchId);
            builder.setDeleteIntent(PendingIntent.getBroadcast(context, 0, dismissedIntent, 0));
            builder.setAutoCancel(true);
        }

        Intent clickIntent = createClickIntent(batchId, batchStatus);
        builder.setContentIntent(PendingIntent.getBroadcast(context, 0, clickIntent, PendingIntent.FLAG_CANCEL_CURRENT));

        customiseNotification(type, builder, batch);
    }

    private Intent createNotificationDismissedIntent(long batchId) {
        Intent hideIntent = new Intent(ACTION_NOTIFICATION_DISMISSED, Uri.EMPTY, context, DownloadReceiver.class);
        hideIntent.putExtra(DownloadReceiver.EXTRA_BATCH_ID, batchId);
        hideIntent.setData(createUniqueUri(hideIntent));
        return hideIntent;
    }

    private Uri createUniqueUri(Intent intent) {
        return Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME));
    }

    private Intent createClickIntent(long batchId, int batchStatus) {
        String action = getActionFrom(batchStatus);
        Intent clickIntent = new Intent(action, Uri.EMPTY, context, DownloadReceiver.class);

        clickIntent.putExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS, new long[]{batchId});
        clickIntent.putExtra(DownloadReceiver.EXTRA_BATCH_ID, batchId);
        int status = statusTranslator.translate(batchStatus);
        clickIntent.putExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_STATUSES, new int[]{status});
        clickIntent.setData(createUniqueUri(clickIntent));

        return clickIntent;
    }

    private String getActionFrom(int batchStatus) {
        if (DownloadStatus.isFailure(batchStatus)) {
            return ACTION_DOWNLOAD_FAILED_CLICK;
        } else if (DownloadStatus.isRunning(batchStatus)) {
            return ACTION_DOWNLOAD_RUNNING_CLICK;
        } else if (DownloadStatus.isSuccess(batchStatus)) {
            return ACTION_DOWNLOAD_SUCCESS_CLICK;
        } else if (DownloadStatus.isCancelled(batchStatus)) {
            return ACTION_DOWNLOAD_CANCELLED_CLICK;
        } else if (DownloadStatus.isSubmitted(batchStatus)) {
            return ACTION_DOWNLOAD_SUBMITTED_CLICK;
        }
        return ACTION_DOWNLOAD_OTHER_CLICK;
    }

    private void customiseNotification(int type, NotificationCompat.Builder builder, DownloadBatch batch) {
        Download download = downloadMarshaller.marshall(batch);
        switch (type) {
            case SynchronisedDownloadNotifier.TYPE_WAITING:
                notificationCustomiser.customiseQueued(download, builder);
                break;
            case SynchronisedDownloadNotifier.TYPE_ACTIVE:
                notificationCustomiser.customiseDownloading(download, builder);
                break;
            case SynchronisedDownloadNotifier.TYPE_SUCCESS:
                notificationCustomiser.customiseComplete(download, builder);
                break;
            case SynchronisedDownloadNotifier.TYPE_CANCELLED:
                notificationCustomiser.customiseCancelled(download, builder);
                break;
            case SynchronisedDownloadNotifier.TYPE_FAILED:
                notificationCustomiser.customiseFailed(download, builder);
                break;
            default:
                throw new IllegalStateException("Deal with this new type " + type);
        }
    }

    private Notification buildTitlesAndDescription(int type, Collection<DownloadBatch> cluster, NotificationCompat.Builder builder) {
        String remainingText = null;
        String percentText = null;
        if (type == SynchronisedDownloadNotifier.TYPE_ACTIVE) {
            int totalPercent = 0;
            long remainingMillis = 0;
            synchronized (downloadSpeed) {
                for (DownloadBatch batch : cluster) {
                    DownloadBatch.Statistics statistics = batch.getLiveStatistics(downloadSpeed);
                    totalPercent += statistics.getPercentComplete();
                    remainingMillis += statistics.getTimeRemaining();
                }
                totalPercent /= cluster.size();
            }

            if (totalPercent > 0) {
                percentText = context.getString(R.string.dl__download_percent, totalPercent);

                if (remainingMillis > 0) {
                    remainingText = context.getString(R.string.dl__duration, formatDuration(remainingMillis));
                }

                builder.setProgress(100, totalPercent, false);
            } else {
                builder.setProgress(100, 0, true);
            }
        }

        List<DownloadBatch> currentBatches = new ArrayList<>();
        for (DownloadBatch batch : cluster) {
            currentBatches.add(batch);
        }

        if (currentBatches.size() == 1) {
            DownloadBatch batch = currentBatches.iterator().next();
            return buildSingleNotification(type, builder, batch, percentText);
        } else {
            return buildStackedNotification(type, builder, currentBatches, remainingText, percentText);
        }
    }

    private Notification buildSingleNotification(
            int type,
            NotificationCompat.Builder builder,
            DownloadBatch batch,
            String percentText) {

        NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle();
        String imageUrl = batch.getBigPictureUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            Bitmap bitmap = imageRetriever.retrieveImage(imageUrl);
            style.bigPicture(bitmap);
        }
        CharSequence title = getDownloadTitle(batch);
        builder.setContentTitle(title);
        style.setBigContentTitle(title);

        if (type == SynchronisedDownloadNotifier.TYPE_ACTIVE) {
            String description = batch.getDescription();
            if (TextUtils.isEmpty(description)) {
                setSecondaryNotificationText(builder, style, context.getString(R.string.dl__downloading));
            } else {
                setSecondaryNotificationText(builder, style, description);
            }
            builder.setContentInfo(percentText);

        } else if (type == SynchronisedDownloadNotifier.TYPE_WAITING) {
            setSecondaryNotificationText(builder, style, context.getString(R.string.dl__download_size_requires_wifi));

        } else if (type == SynchronisedDownloadNotifier.TYPE_SUCCESS) {
            setSecondaryNotificationText(builder, style, context.getString(R.string.dl__download_complete));
        } else if (type == SynchronisedDownloadNotifier.TYPE_FAILED) {
            setSecondaryNotificationText(builder, style, context.getString(R.string.dl__download_unsuccessful));
        } else if (type == SynchronisedDownloadNotifier.TYPE_CANCELLED) {
            setSecondaryNotificationText(builder, style, context.getString(R.string.dl__download_cancelled));
        }

        if (!TextUtils.isEmpty(imageUrl)) {
            builder.setStyle(style);
        }
        return builder.build();
    }

    private CharSequence getDownloadTitle(DownloadBatch batch) {
        String title = batch.getTitle();
        if (TextUtils.isEmpty(title)) {
            return context.getString(R.string.dl__title_unknown);
        } else {
            return title;
        }
    }

    private void setSecondaryNotificationText(NotificationCompat.Builder builder, NotificationCompat.BigPictureStyle style, String description) {
        builder.setContentText(description);
        style.setSummaryText(description);
    }

    private Notification buildStackedNotification(
            int type,
            NotificationCompat.Builder builder,
            Collection<DownloadBatch> currentBatches,
            String remainingText,
            String percentText) {

        final NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle(builder);

        for (DownloadBatch batch : currentBatches) {
            inboxStyle.addLine(getDownloadTitle(batch));
        }

        if (type == SynchronisedDownloadNotifier.TYPE_ACTIVE) {
            builder.setContentTitle(resources.getQuantityString(R.plurals.dl__notif_summary_active, currentBatches.size(), currentBatches.size()));
            builder.setContentInfo(percentText);
            setSecondaryNotificationText(builder, inboxStyle, remainingText);
        } else if (type == SynchronisedDownloadNotifier.TYPE_WAITING) {
            builder.setContentTitle(resources.getQuantityString(R.plurals.dl__notif_summary_waiting, currentBatches.size(), currentBatches.size()));
            setSecondaryNotificationText(builder, inboxStyle, context.getString(R.string.dl__download_size_requires_wifi));
        } else if (type == SynchronisedDownloadNotifier.TYPE_SUCCESS) {
            setSecondaryNotificationText(builder, inboxStyle, context.getString(R.string.dl__download_complete));
        } else if (type == SynchronisedDownloadNotifier.TYPE_FAILED) {
            setSecondaryNotificationText(builder, inboxStyle, context.getString(R.string.dl__download_unsuccessful));
        } else if (type == SynchronisedDownloadNotifier.TYPE_CANCELLED) {
            setSecondaryNotificationText(builder, inboxStyle, context.getString(R.string.dl__download_cancelled));
        }

        return inboxStyle.build();
    }

    private void setSecondaryNotificationText(NotificationCompat.Builder builder, NotificationCompat.InboxStyle style, String description) {
        builder.setContentText(description);
        style.setSummaryText(description);
    }

    public void notifyDownloadSpeed(long id, long bytesPerSecond) {
        synchronized (downloadSpeed) {
            if (bytesPerSecond != 0) {
                downloadSpeed.put(id, bytesPerSecond);
            } else {
                downloadSpeed.remove(id);
            }
        }
    }

    /**
     * Return given duration in a human-friendly format. For example, "4
     * minutes" or "1 second". Returns only largest meaningful unit of time,
     * from seconds up to hours.
     */
    private CharSequence formatDuration(long millis) {
        if (millis >= DateUtils.HOUR_IN_MILLIS) {
            int hours = (int) TimeUnit.MILLISECONDS.toHours(millis + TimeUnit.MINUTES.toMillis(30));
            return resources.getQuantityString(R.plurals.dl__duration_hours, hours, hours);
        } else if (millis >= DateUtils.MINUTE_IN_MILLIS) {
            int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(millis + TimeUnit.SECONDS.toMillis(30));
            return resources.getQuantityString(R.plurals.dl__duration_minutes, minutes, minutes);
        } else {
            int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(millis + 500);
            return resources.getQuantityString(R.plurals.dl__duration_seconds, seconds, seconds);
        }
    }

    public void cancelStaleTags(List<Integer> staleTagsToBeRemoved) {
        for (Integer tag : staleTagsToBeRemoved) {
            notificationManager.cancel(tag);
        }
    }

    public void cancelAll() {
        notificationManager.cancelAll();
    }

}
