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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;

import com.novoda.notils.logger.simple.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Update {@link NotificationManager} to reflect current {@link DownloadInfo}
 * states. Collapses similar downloads into a single notification, and builds
 * {@link PendingIntent} that launch towards {DownloadReceiver}.
 */
class DownloadNotifier {

    static final int TYPE_ACTIVE = 1;
    static final int TYPE_WAITING = 2;
    static final int TYPE_SUCCESS = 3;
    static final int TYPE_FAILED = 4;
    static final int TYPE_CANCELLED = 5;

    private final Context context;

    /**
     * Currently active notifications, mapped from clustering tag to timestamp
     * when first shown.
     *
     * @see #buildNotificationTag(DownloadBatch)
     */
    private final SimpleArrayMap<String, Long> activeNotifications = new SimpleArrayMap<>();

    private final NotificationDisplayer notificationDisplayer;

    public DownloadNotifier(Context context, NotificationDisplayer notificationDisplayer) {
        this.context = context;
        this.notificationDisplayer = notificationDisplayer;
    }

    public void cancelAll() {
        notificationDisplayer.cancelAll();
    }

    /**
     * Notify the current speed of an active download, used for calculating
     * estimated remaining time.
     */
    public void notifyDownloadSpeed(long id, long bytesPerSecond) {
        notificationDisplayer.notifyDownloadSpeed(id, bytesPerSecond);
    }

    /**
     * Update {@link NotificationManager} to reflect the given set of
     * {@link DownloadInfo}, adding, collapsing, and removing as needed.
     */
    public void updateWith(Collection<DownloadBatch> batches) {
        synchronized (activeNotifications) {
            SimpleArrayMap<String, Collection<DownloadBatch>> clusters = getClustersByNotificationTag(batches);

            for (int i = 0, size = clusters.size(); i < size; i++) {
                String notificationId = clusters.keyAt(i);
                long firstShown = getFirstShownTime(notificationId);
                notificationDisplayer.buildAndShowNotification(clusters, notificationId, firstShown);
            }

            List<Integer> staleTagsToBeRemoved = getStaleTagsThatWereNotRenewed(clusters);
            notificationDisplayer.cancelStaleTags(staleTagsToBeRemoved);
        }
    }

    private long getFirstShownTime(String notificationId) {
        final long firstShown;
        if (activeNotifications.containsKey(notificationId)) {
            firstShown = activeNotifications.get(notificationId);
        } else {
            firstShown = System.currentTimeMillis();
            activeNotifications.put(notificationId, firstShown);
        }
        return firstShown;
    }

    @NonNull
    private SimpleArrayMap<String, Collection<DownloadBatch>> getClustersByNotificationTag(Collection<DownloadBatch> batches) {
        SimpleArrayMap<String, Collection<DownloadBatch>> clustered = new SimpleArrayMap<>();

        for (DownloadBatch batch : batches) {
            String tag = buildNotificationTag(batch);

            addBatchToCluster(tag, clustered, batch);
        }

        return clustered;
    }

    /**
     * Build tag used for collapsing several {@link DownloadInfo} into a single
     * {@link Notification}.
     */
    private String buildNotificationTag(DownloadBatch batch) {
        int status = batch.getStatus();
        int visibility = batch.getInfo().getVisibility();
        if (status == Downloads.Impl.STATUS_QUEUED_FOR_WIFI) {
            return TYPE_WAITING + ":" + context.getPackageName();
        } else if (status == Downloads.Impl.STATUS_RUNNING && shouldShowActiveItem(visibility)) {
            return TYPE_ACTIVE + ":" + context.getPackageName();
        } else if (Downloads.Impl.isStatusError(status) && !Downloads.Impl.isStatusCancelled(status)
                && shouldShowCompletedItem(visibility)) {
            // Failed downloads always have unique notifs
            return TYPE_FAILED + ":" + batch.getBatchId();
        } else if (Downloads.Impl.isStatusCancelled(status) && shouldShowCompletedItem(visibility)) {
            // Cancelled downloads always have unique notifs
            return TYPE_CANCELLED + ":" + batch.getBatchId();
        } else if (Downloads.Impl.isStatusSuccess(status) && shouldShowCompletedItem(visibility)) {
            // Complete downloads always have unique notifs
            return TYPE_SUCCESS + ":" + batch.getBatchId();
        } else {
            return null;
        }
    }

    private boolean shouldShowActiveItem(int visibility) {
        return visibility == NotificationVisibility.ONLY_WHEN_ACTIVE
                || visibility == NotificationVisibility.ACTIVE_OR_COMPLETE;
    }

    private boolean shouldShowCompletedItem(int visibility) {
        return visibility == NotificationVisibility.ONLY_WHEN_COMPLETE
                || visibility == NotificationVisibility.ACTIVE_OR_COMPLETE;
    }

    private void addBatchToCluster(String tag, SimpleArrayMap<String, Collection<DownloadBatch>> cluster, DownloadBatch batch) {
        if (tag == null) {
            return;
        }

        Collection<DownloadBatch> batches;

        if (cluster.containsKey(tag)) {
            batches = cluster.get(tag);
        } else {
            batches = new ArrayList<>();
            cluster.put(tag, batches);
        }

        batches.add(batch);
    }

    private List<Integer> getStaleTagsThatWereNotRenewed(SimpleArrayMap<String, Collection<DownloadBatch>> clustered) {
        List<Integer> staleTags = new ArrayList<>();

        for (int i = activeNotifications.size() - 1; i >= 0; i--) {
            String tag = activeNotifications.keyAt(i);
            if (!clustered.containsKey(tag)) {
                staleTags.add(tag.hashCode());
                activeNotifications.removeAt(i);
            }
        }

        return staleTags;
    }

    public void dumpSpeeds() {
        Log.e("dump at speed");
    }
}
