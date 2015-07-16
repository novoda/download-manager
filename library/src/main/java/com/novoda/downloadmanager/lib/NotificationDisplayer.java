package com.novoda.downloadmanager.lib;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.novoda.downloadmanager.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

class NotificationDisplayer {
    private final Context context;
    private final NotificationManager notificationManager;
    private final NotificationImageRetriever imageRetriever;
    private final Resources resources;
    private final DownloadsUriProvider downloadsUriProvider;

    /**
     * Current speed of active downloads, mapped from {@link FileDownloadInfo#batchId}
     * to speed in bytes per second.
     */
    private final LongSparseArray<Long> downloadSpeed = new LongSparseArray<>();

    public NotificationDisplayer(
            Context context,
            NotificationManager notificationManager,
            NotificationImageRetriever imageRetriever,
            Resources resources,
            DownloadsUriProvider downloadsUriProvider) {
        this.context = context;
        this.notificationManager = notificationManager;
        this.imageRetriever = imageRetriever;
        this.resources = resources;
        this.downloadsUriProvider = downloadsUriProvider;
    }

    public void buildAndShowNotification(SimpleArrayMap<String, Collection<DownloadBatch>> clusters, String notificationId, long firstShown) {
        int type = getNotificationTagType(notificationId);
        Collection<DownloadBatch> cluster = clusters.get(notificationId);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setWhen(firstShown);
        buildIcon(type, builder);
        buildActionIntents(notificationId, type, cluster, builder);

        Notification notification = buildTitlesAndDescription(type, cluster, builder);
        notificationManager.notify(notificationId.hashCode(), notification);
    }

    /**
     * Return the cluster type of the given as created by
     * {@link DownloadNotifier#buildNotificationTag(DownloadBatch)}.
     */
    private int getNotificationTagType(String tag) {
        return Integer.parseInt(tag.substring(0, tag.indexOf(':')));
    }

    private void buildIcon(int type, NotificationCompat.Builder builder) {
        switch (type) {
            case DownloadNotifier.TYPE_ACTIVE:
                builder.setSmallIcon(android.R.drawable.stat_sys_download);
                break;
            case DownloadNotifier.TYPE_WAITING:
            case DownloadNotifier.TYPE_FAILED:
                builder.setSmallIcon(android.R.drawable.stat_sys_warning);
                break;
            case DownloadNotifier.TYPE_SUCCESS:
                builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
                break;
            default:
                builder.setSmallIcon(android.R.drawable.stat_sys_warning);
                break;
        }
    }

    private void buildActionIntents(String tag, int type, Collection<DownloadBatch> cluster, NotificationCompat.Builder builder) {
        if (type == DownloadNotifier.TYPE_ACTIVE || type == DownloadNotifier.TYPE_WAITING) {
            // build a synthetic uri for intent identification purposes
            Uri uri = new Uri.Builder().scheme("active-dl").appendPath(tag).build();
            Intent clickIntent = new Intent(Constants.ACTION_LIST, uri, context, DownloadReceiver.class);
            clickIntent.putExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS, getDownloadIds(cluster));
            builder.setContentIntent(PendingIntent.getBroadcast(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT));
            builder.setOngoing(true);

            DownloadBatch batch = cluster.iterator().next();
            Intent cancelIntent = new Intent(Constants.ACTION_CANCEL, null, context, DownloadReceiver.class);
            cancelIntent.putExtra(DownloadReceiver.EXTRA_BATCH_ID, batch.getBatchId());
            PendingIntent pendingCancelIntent = PendingIntent.getBroadcast(context, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.dl__ic_action_cancel, context.getString(R.string.dl__cancel), pendingCancelIntent);

        } else if (type == DownloadNotifier.TYPE_SUCCESS) {
            DownloadBatch batch = cluster.iterator().next();
            // TODO: Decide how we handle notification clicks
            FileDownloadInfo fileDownloadInfo = batch.getDownloads().get(0);
            Uri uri = ContentUris.withAppendedId(downloadsUriProvider.getAllDownloadsUri(), fileDownloadInfo.getId());
            builder.setAutoCancel(true);

            final String action;
            if (DownloadStatus.isError(batch.getStatus())) {
                action = Constants.ACTION_LIST;
            } else {
                action = Constants.ACTION_OPEN;
            }

            final Intent intent = new Intent(action, uri, context, DownloadReceiver.class);
            intent.putExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS, getDownloadIds(cluster));
            intent.putExtra(DownloadReceiver.EXTRA_BATCH_ID, batch.getBatchId());
            builder.setContentIntent(PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));

            final Intent hideIntent = new Intent(Constants.ACTION_HIDE, uri, context, DownloadReceiver.class);
            hideIntent.putExtra(DownloadReceiver.EXTRA_BATCH_ID, batch.getBatchId());
            builder.setDeleteIntent(PendingIntent.getBroadcast(context, 0, hideIntent, 0));
        }
    }

    private Notification buildTitlesAndDescription(int type, Collection<DownloadBatch> cluster, NotificationCompat.Builder builder) {
        String remainingText = null;
        String percentText = null;
        if (type == DownloadNotifier.TYPE_ACTIVE) {
            long currentBytes = 0;
            long totalBytes = 0;
            long totalBytesPerSecond = 0;
            synchronized (downloadSpeed) {
                for (DownloadBatch batch : cluster) {
                    for (FileDownloadInfo info : batch.getDownloads()) {
                        if (info.hasTotalBytes()) {
                            currentBytes += info.getCurrentBytes();
                            totalBytes += info.getTotalBytes();
                            Long bytesPerSecond = downloadSpeed.get(info.getId());
                            if (bytesPerSecond != null) {
                                totalBytesPerSecond += bytesPerSecond;
                            }
                        }
                    }
                }
            }

            if (totalBytes > 0) {
                int percent = (int) ((currentBytes * 100) / totalBytes);
                percentText = context.getString(R.string.dl__download_percent, percent);

                if (totalBytesPerSecond > 0) {
                    long remainingMillis = ((totalBytes - currentBytes) * 1000) / totalBytesPerSecond;
                    remainingText = context.getString(R.string.dl__duration, formatDuration(remainingMillis));
                }

                builder.setProgress(100, percent, false);
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
            return buildSingleNotification(type, builder, batch, remainingText, percentText);

        } else {
            return buildStackedNotification(type, builder, currentBatches, remainingText, percentText);
        }
    }

    private Notification buildSingleNotification(
            int type,
            NotificationCompat.Builder builder,
            DownloadBatch batch, String remainingText,
            String percentText) {

        NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle();
        String imageUrl = batch.getInfo().getBigPictureUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            Bitmap bitmap = imageRetriever.retrieveImage(imageUrl);
            style.bigPicture(bitmap);
        }
        CharSequence title = getDownloadTitle(batch.getInfo());
        builder.setContentTitle(title);
        style.setBigContentTitle(title);

        if (type == DownloadNotifier.TYPE_ACTIVE) {
            String description = batch.getInfo().getDescription();
            if (TextUtils.isEmpty(description)) {
                setSecondaryNotificationText(builder, style, remainingText);
            } else {
                setSecondaryNotificationText(builder, style, description);
            }
            builder.setContentInfo(percentText);

        } else if (type == DownloadNotifier.TYPE_WAITING) {
            setSecondaryNotificationText(builder, style, context.getString(R.string.dl__download_size_requires_wifi));

        } else if (type == DownloadNotifier.TYPE_SUCCESS) {
            setSecondaryNotificationText(builder, style, context.getString(R.string.dl__download_complete));
        } else if (type == DownloadNotifier.TYPE_FAILED) {
            setSecondaryNotificationText(builder, style, context.getString(R.string.dl__download_unsuccessful));
        } else if (type == DownloadNotifier.TYPE_CANCELLED) {
            setSecondaryNotificationText(builder, style, context.getString(R.string.dl__download_cancelled));
        }

        if (!TextUtils.isEmpty(imageUrl)) {
            builder.setStyle(style);
        }
        return builder.build();
    }

    private CharSequence getDownloadTitle(BatchInfo batch) {
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
            inboxStyle.addLine(getDownloadTitle(batch.getInfo()));
        }

        if (type == DownloadNotifier.TYPE_ACTIVE) {
            builder.setContentTitle(resources.getQuantityString(R.plurals.dl__notif_summary_active, currentBatches.size(), currentBatches.size()));
            builder.setContentInfo(percentText);
            setSecondaryNotificationText(builder, inboxStyle, remainingText);
        } else if (type == DownloadNotifier.TYPE_WAITING) {
            builder.setContentTitle(resources.getQuantityString(R.plurals.dl__notif_summary_waiting, currentBatches.size(), currentBatches.size()));
            setSecondaryNotificationText(builder, inboxStyle, context.getString(R.string.dl__download_size_requires_wifi));
        } else if (type == DownloadNotifier.TYPE_SUCCESS) {
            setSecondaryNotificationText(builder, inboxStyle, context.getString(R.string.dl__download_complete));
        } else if (type == DownloadNotifier.TYPE_FAILED) {
            setSecondaryNotificationText(builder, inboxStyle, context.getString(R.string.dl__download_unsuccessful));
        } else if (type == DownloadNotifier.TYPE_CANCELLED) {
            setSecondaryNotificationText(builder, inboxStyle, context.getString(R.string.dl__download_cancelled));
        }

        return inboxStyle.build();
    }

    private void setSecondaryNotificationText(NotificationCompat.Builder builder, NotificationCompat.InboxStyle style, String description) {
        builder.setContentText(description);
        style.setSummaryText(description);
    }

    private long[] getDownloadIds(Collection<DownloadBatch> batches) {
        List<Long> ids = new ArrayList<>();
        for (DownloadBatch batch : batches) {
            for (FileDownloadInfo fileDownloadInfo : batch.getDownloads()) {
                ids.add(fileDownloadInfo.getId());
            }
        }

        long[] idArray = new long[ids.size()];

        for (int i = 0, idsSize = ids.size(); i < idsSize; i++) {
            idArray[i] = ids.get(i);
        }
        return idArray;
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
