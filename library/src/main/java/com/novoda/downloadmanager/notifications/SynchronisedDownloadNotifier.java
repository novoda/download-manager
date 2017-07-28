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

package com.novoda.downloadmanager.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.util.SimpleArrayMap;

import com.novoda.downloadmanager.lib.DownloadBatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Update {@link NotificationManager} to reflect current {@link DownloadBatch} states.
 * Collapses similar downloads into a single notification, and builds
 * {@link PendingIntent} that launch towards {DownloadReceiver}.
 */
class SynchronisedDownloadNotifier implements DownloadNotifier {

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

    public SynchronisedDownloadNotifier(Context context, NotificationDisplayer notificationDisplayer) {
        this.context = context;
        this.notificationDisplayer = notificationDisplayer;
    }

    @Override
    public void cancelAll() {
        notificationDisplayer.cancelAll();
    }

    /**
     * Notify the current speed of an active download, used for calculating
     * estimated remaining time.
     */
    @Override
    public void notifyDownloadSpeed(long id, long bytesPerSecond) {
        notificationDisplayer.notifyDownloadSpeed(id, bytesPerSecond);
    }

    /**
     * Update Notifications to reflect the given set of
     * {@link DownloadBatch}, adding, collapsing, and removing as needed.
     */
    @Override
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

    private SimpleArrayMap<String, Collection<DownloadBatch>> getClustersByNotificationTag(Collection<DownloadBatch> batches) {
        SimpleArrayMap<String, Collection<DownloadBatch>> taggedBatches = new SimpleArrayMap<>();

        for (DownloadBatch batch : batches) {
            String tag = buildNotificationTag(batch);

            associateBatchesWithTag(tag, batch, taggedBatches);
        }

        return taggedBatches;
    }

    /**
     * Build tag used for collapsing several {@link DownloadBatch} into a single
     * {@link Notification}.
     */
    private String buildNotificationTag(DownloadBatch batch) {
        // TODO this method and NotificationDisplayer.#getNotificationTagType have an inherent contract
        // If we pulled out a `NotificationTag` value object this would fix it
        if (batch.isQueuedForWifi()) {
            return TYPE_WAITING + ":" + context.getPackageName();
        } else if (batch.isRunning() && batch.shouldShowActiveItem()) {
            return TYPE_ACTIVE + ":" + context.getPackageName();
        } else if (batch.isError() && !batch.isCancelled() && batch.shouldShowCompletedItem()) {
            // Failed downloads always have unique notifications
            return TYPE_FAILED + ":" + batch.getBatchId();
        } else if (batch.isCancelled() && batch.shouldShowCompletedItem()) {
            // Cancelled downloads always have unique notifications
            return TYPE_CANCELLED + ":" + batch.getBatchId();
        } else if (batch.isSuccess() && batch.shouldShowCompletedItem()) {
            // Complete downloads always have unique notifications
            return TYPE_SUCCESS + ":" + batch.getBatchId();
        } else {
            return null;
        }
    }

    private void associateBatchesWithTag(String tag, DownloadBatch batch, SimpleArrayMap<String, Collection<DownloadBatch>> taggedBatches) {
        if (tag == null) {
            return;
        }

        Collection<DownloadBatch> batchesForTag;

        if (taggedBatches.containsKey(tag)) {
            batchesForTag = taggedBatches.get(tag);
        } else {
            batchesForTag = new ArrayList<>();
            taggedBatches.put(tag, batchesForTag);
        }

        batchesForTag.add(batch);
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

}
