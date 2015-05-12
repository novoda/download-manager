/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.novoda.downloadmanager.lib;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.novoda.downloadmanager.R;
import com.novoda.notils.logger.simple.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.novoda.downloadmanager.lib.Request.*;

/**
 * Update {@link NotificationManager} to reflect current {@link DownloadInfo}
 * states. Collapses similar downloads into a single notification, and builds
 * {@link PendingIntent} that launch towards {DownloadReceiver}.
 */
public class DownloadNotifier {

    private static final int TYPE_ACTIVE = 1;
    private static final int TYPE_WAITING = 2;
    private static final int TYPE_COMPLETE = 3;

    private final Context mContext;
    private final NotificationImageRetriever imageRetriever;
    private final NotificationManager mNotifManager;

    /**
     * Currently active notifications, mapped from clustering tag to timestamp
     * when first shown.
     *
     * @see #buildNotificationTag(DownloadInfo)
     */
//    @GuardedBy("mActiveNotifs")
    private final HashMap<String, Long> mActiveNotifs = new HashMap<String, Long>();

    /**
     * Current speed of active downloads, mapped from {@link DownloadInfo#mId}
     * to speed in bytes per second.
     */
//    @GuardedBy("mDownloadSpeed")
    // LongSparseLongArray
    private final LongSparseArray<Long> mDownloadSpeed = new LongSparseArray<Long>();

    /**
     * Last time speed was reproted, mapped from {@link DownloadInfo#mId} to
     * {@link SystemClock#elapsedRealtime()}.
     */
//    @GuardedBy("mDownloadSpeed")
    // LongSparseLongArray
    private final LongSparseArray<Long> mDownloadTouch = new LongSparseArray<Long>();

    public DownloadNotifier(Context context, NotificationImageRetriever imageRetriever) {
        this.mContext = context;
        this.imageRetriever = imageRetriever;
        this.mNotifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void cancelAll() {
        mNotifManager.cancelAll();
    }

    /**
     * Notify the current speed of an active download, used for calculating
     * estimated remaining time.
     */
    public void notifyDownloadSpeed(long id, long bytesPerSecond) {
        synchronized (mDownloadSpeed) {
            if (bytesPerSecond != 0) {
                mDownloadSpeed.put(id, bytesPerSecond);
                mDownloadTouch.put(id, SystemClock.elapsedRealtime());
            } else {
                mDownloadSpeed.remove(id);
                mDownloadTouch.remove(id);
            }
        }
    }

    /**
     * Update {@link NotificationManager} to reflect the given set of
     * {@link DownloadInfo}, adding, collapsing, and removing as needed.
     */
    public void updateWith(Collection<DownloadInfo> downloads) {
        synchronized (mActiveNotifs) {
            updateWithLocked(downloads);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void updateWithLocked(Collection<DownloadInfo> downloads) {
        final Resources res = mContext.getResources();
        final Map<String, List<DownloadInfo>> clustered = new HashMap<String, List<DownloadInfo>>();

        for (DownloadInfo info : downloads) {
            final String tag = buildNotificationTag(info);
            addInfoToCluster(tag, clustered, info);
        }

        // Build notification for each cluster
        for (String tag : clustered.keySet()) {
            final int type = getNotificationTagType(tag);
            final Collection<DownloadInfo> cluster = clustered.get(tag);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
            useTimeWhenClusterFirstShownToAvoidShuffling(tag, builder);
            buildIcon(type, builder);
            buildActionIntents(tag, type, cluster, builder);

            Notification notification = buildTitlesAndDescription(res, type, cluster, builder);
            mNotifManager.notify(0, notification);
        }

        removeStaleTagsThatWerentRenewed(clustered);
    }

    private void addInfoToCluster(final String tag, final Map<String, List<DownloadInfo>> cluster, final DownloadInfo info) {
        if (tag == null) {
            return;
        }

        List<DownloadInfo> downloadInfos;

        if (cluster.containsKey(tag)) {
            downloadInfos = cluster.get(tag);
        } else {
            downloadInfos = new ArrayList<DownloadInfo>();
            cluster.put(tag, downloadInfos); // TODO not sure if this is right compared to ArrayListMultiMap
        }

        downloadInfos.add(info);
    }

    private void useTimeWhenClusterFirstShownToAvoidShuffling(String tag, NotificationCompat.Builder builder) {
        final long firstShown;
        if (mActiveNotifs.containsKey(tag)) {
            firstShown = mActiveNotifs.get(tag);
        } else {
            firstShown = System.currentTimeMillis();
            mActiveNotifs.put(tag, firstShown);
        }
        builder.setWhen(firstShown);
    }

    private void buildIcon(int type, NotificationCompat.Builder builder) {
        if (type == TYPE_ACTIVE) {
            builder.setSmallIcon(android.R.drawable.stat_sys_download);
        } else if (type == TYPE_WAITING) {
            builder.setSmallIcon(android.R.drawable.stat_sys_warning);
        } else if (type == TYPE_COMPLETE) {
            builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
        }
    }

    private void buildActionIntents(String tag, int type, Collection<DownloadInfo> cluster, NotificationCompat.Builder builder) {
        if (type == TYPE_ACTIVE || type == TYPE_WAITING) {
            // build a synthetic uri for intent identification purposes
            Uri uri = new Uri.Builder().scheme("active-dl").appendPath(tag).build();
            Intent clickIntent = new Intent(Constants.ACTION_LIST, uri, mContext, DownloadReceiver.class);
            clickIntent.putExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS, getDownloadIds(cluster));
            builder.setContentIntent(PendingIntent.getBroadcast(mContext, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT));
            builder.setOngoing(true);

            DownloadInfo info = cluster.iterator().next();
            uri = ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, info.mId);
            Intent cancelIntent = new Intent(Constants.ACTION_CANCEL, uri, mContext, DownloadReceiver.class);
            PendingIntent pendingCancelIntent = PendingIntent.getBroadcast(mContext, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.dl__ic_action_cancel, "Cancel", pendingCancelIntent);

        } else if (type == TYPE_COMPLETE) {
            final DownloadInfo info = cluster.iterator().next();
            final Uri uri = ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, info.mId);
            builder.setAutoCancel(true);

            final String action;
            if (Downloads.Impl.isStatusError(info.mStatus)) {
                action = Constants.ACTION_LIST;
            } else {
                if (info.mDestination != Downloads.Impl.DESTINATION_SYSTEMCACHE_PARTITION) {
                    action = Constants.ACTION_OPEN;
                } else {
                    action = Constants.ACTION_LIST;
                }
            }

            final Intent intent = new Intent(action, uri, mContext, DownloadReceiver.class);
            intent.putExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS, getDownloadIds(cluster));
            builder.setContentIntent(PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));

            final Intent hideIntent = new Intent(Constants.ACTION_HIDE, uri, mContext, DownloadReceiver.class);
            builder.setDeleteIntent(PendingIntent.getBroadcast(mContext, 0, hideIntent, 0));
        }
    }

    private Notification buildTitlesAndDescription(Resources res, int type, Collection<DownloadInfo> cluster, NotificationCompat.Builder builder) {
        String remainingText = null;
        String percentText = null;
        if (type == TYPE_ACTIVE) {
            long current = 0;
            long total = 0;
            long speed = 0;
            synchronized (mDownloadSpeed) {
                for (DownloadInfo info : cluster) {
                    if (mDownloadSpeed.get(info.mId) == null) {
                        continue;
                    }
                    if (info.mTotalBytes != -1) {
                        current += info.mCurrentBytes;
                        total += info.mTotalBytes;
                        speed += mDownloadSpeed.get(info.mId);
                    }
                }
            }

            if (total > 0) {
                final int percent = (int) ((current * 100) / total);
                percentText = percent + "%";//res.getString(R.string.download_percent, percent);

                if (speed > 0) {
                    final long remainingMillis = ((total - current) * 1000) / speed;
                    remainingText = "Duration " + formatDuration(remainingMillis);
                }

                builder.setProgress(100, percent, false);
            } else {
                builder.setProgress(100, 0, true);
            }
        }

        if (cluster.size() == 1) {
            final DownloadInfo info = cluster.iterator().next();

            final NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle();
            String imageUrl = info.bigPictureResourceUrl;
            if (!TextUtils.isEmpty(imageUrl)) {
                Bitmap bitmap = imageRetriever.retrieveImage(imageUrl);
                style.bigPicture(bitmap);
            }
            builder.setContentTitle(getDownloadTitle(res, info));
            style.setBigContentTitle(getDownloadTitle(res, info));

            if (type == TYPE_ACTIVE) {
                if (!TextUtils.isEmpty(info.mDescription)) {
                    builder.setContentText(info.mDescription);
                    style.setSummaryText(info.mDescription);
                } else {
                    builder.setContentText(remainingText);
                    style.setSummaryText(remainingText);
                }
                builder.setContentInfo(percentText);

            } else if (type == TYPE_WAITING) {
                builder.setContentText("Download size requires Wi-Fi.");
                style.setSummaryText("Download size requires Wi-Fi.");

            } else if (type == TYPE_COMPLETE) {
                if (Downloads.Impl.isStatusError(info.mStatus)) {
                    builder.setContentText("Download unsuccessful.");
                    style.setSummaryText("Download unsuccessful.");
                } else if (Downloads.Impl.isStatusSuccess(info.mStatus)) {
                    builder.setContentText("Download complete.");
                    style.setSummaryText("Download complete.");
                }
            }

            if (!TextUtils.isEmpty(imageUrl)) {
                builder.setStyle(style);
            }
            return builder.build();

        } else

        {
            final NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle(builder);

            for (DownloadInfo info : cluster) {
                inboxStyle.addLine(getDownloadTitle(res, info));
            }

            if (type == TYPE_ACTIVE) {
                builder.setContentTitle(res.getQuantityString(R.plurals.dl__notif_summary_active, cluster.size(), cluster.size()));
                builder.setContentText(remainingText);
                builder.setContentInfo(percentText);
                inboxStyle.setSummaryText(remainingText);

            } else if (type == TYPE_WAITING) {
                builder.setContentTitle(res.getQuantityString(R.plurals.dl__notif_summary_waiting, cluster.size(), cluster.size()));
                builder.setContentText("Download size requires Wi-Fi.");
                inboxStyle.setSummaryText("Download size requires Wi-Fi.");
            }

            return inboxStyle.build();
        }

    }

    private void removeStaleTagsThatWerentRenewed(Map<String, List<DownloadInfo>> clustered) {
        final Iterator<String> it = mActiveNotifs.keySet().iterator();
        while (it.hasNext()) {
            final String tag = it.next();
            if (!clustered.containsKey(tag)) {
                mNotifManager.cancel(0);
                it.remove();
            }
        }
    }

    private static CharSequence getDownloadTitle(Resources res, DownloadInfo info) {
        if (!TextUtils.isEmpty(info.mTitle)) {
            return info.mTitle;
        } else {
            return "unknown";
        }
    }

    private long[] getDownloadIds(Collection<DownloadInfo> infos) {
        final long[] ids = new long[infos.size()];
        int i = 0;
        for (DownloadInfo info : infos) {
            ids[i++] = info.mId;
        }
        return ids;
    }

    public void dumpSpeeds() {
        Log.e("dump at speed");
    }

    /**
     * Build tag used for collapsing several {@link DownloadInfo} into a single
     * {@link Notification}.
     */
    private String buildNotificationTag(DownloadInfo info) {
        if (info.mStatus == Downloads.Impl.STATUS_QUEUED_FOR_WIFI) {
            return TYPE_WAITING + ":" + getPackageName();
        } else if (isActiveAndVisible(info)) {
            return TYPE_ACTIVE + ":" + getPackageName();
        } else if (isCompleteAndVisible(info)) {
            // Complete downloads always have unique notifs
            return TYPE_COMPLETE + ":" + info.mId;
        } else {
            return null;
        }
    }

    private String getPackageName() {
        return mContext.getPackageName();
    }

    /**
     * Return the cluster type of the given as created by
     * {@link #buildNotificationTag(DownloadInfo)}.
     */
    private static int getNotificationTagType(String tag) {
        return Integer.parseInt(tag.substring(0, tag.indexOf(':')));
    }

    private static boolean isActiveAndVisible(DownloadInfo download) {
        return download.mStatus == Downloads.Impl.STATUS_RUNNING &&
                (download.mVisibility == VISIBILITY_VISIBLE
                        || download.mVisibility == VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
    }

    private static boolean isCompleteAndVisible(DownloadInfo download) {
        return Downloads.Impl.isStatusCompleted(download.mStatus) &&
                (download.mVisibility == VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                        || download.mVisibility == VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION);
    }

    /**
     * Return given duration in a human-friendly format. For example, "4
     * minutes" or "1 second". Returns only largest meaningful unit of time,
     * from seconds up to hours.
     */
    public CharSequence formatDuration(long millis) {
        final Resources res = mContext.getResources();
        if (millis >= DateUtils.HOUR_IN_MILLIS) {
            final int hours = (int) ((millis + 1800000) / DateUtils.HOUR_IN_MILLIS);
            return res.getQuantityString(R.plurals.dl__duration_hours, hours, hours);
        } else if (millis >= DateUtils.MINUTE_IN_MILLIS) {
            final int minutes = (int) ((millis + 30000) / DateUtils.MINUTE_IN_MILLIS);
            return res.getQuantityString(R.plurals.dl__duration_minutes, minutes, minutes);
        } else {
            final int seconds = (int) ((millis + 500) / DateUtils.SECOND_IN_MILLIS);
            return res.getQuantityString(R.plurals.dl__duration_seconds, seconds, seconds);
        }
    }
}
