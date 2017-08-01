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
import com.novoda.downloadmanager.lib.NotificationsCreatedListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Update {@link NotificationManager} to reflect current {@link DownloadBatch} states.
 * Collapses similar downloads into a single notification, and builds
 * {@link PendingIntent} that launch towards {DownloadReceiver}.
 */
class SynchronisedDownloadNotifier implements DownloadNotifier {

    private final Context context;

    /**
     * Currently active notifications, mapped from clustering tag to timestamp
     * when first shown.
     *
     * @see NotificationTag#create(DownloadBatch, String)
     */
    private final SimpleArrayMap<NotificationTag, Long> activeNotifications = new SimpleArrayMap<>();

    private final NotificationDisplayer notificationDisplayer;

    SynchronisedDownloadNotifier(Context context, NotificationDisplayer notificationDisplayer) {
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
    public void updateWith(Collection<DownloadBatch> batches, NotificationsCreatedListener notificationsCreatedListener) {
        synchronized (activeNotifications) {
            SimpleArrayMap<NotificationTag, Collection<DownloadBatch>> clusteredBatches = clusterBatchesByNotificationTag(batches);
            SimpleArrayMap<NotificationTag, Notification> taggedNotifications = new SimpleArrayMap<>(activeNotifications.size());

            for (int i = 0, size = clusteredBatches.size(); i < size; i++) {
                NotificationTag notificationTag = clusteredBatches.keyAt(i);
                Collection<DownloadBatch> batchesForTag = clusteredBatches.get(notificationTag);
                long firstShown = getFirstShownTime(notificationTag);
                Notification notification = notificationDisplayer.buildAndShowNotification(notificationTag, batchesForTag, firstShown);
                taggedNotifications.put(notificationTag, notification);
            }

            List<Integer> staleTagsToBeRemoved = getStaleTagsThatWereNotRenewed(clusteredBatches);
            notificationDisplayer.cancelStaleTags(staleTagsToBeRemoved);

            notificationsCreatedListener.onNotificationCreated(taggedNotifications);
        }
    }

    private long getFirstShownTime(NotificationTag tag) {
        final long firstShown;
        if (activeNotifications.containsKey(tag)) {
            firstShown = activeNotifications.get(tag);
        } else {
            firstShown = System.currentTimeMillis();
            activeNotifications.put(tag, firstShown);
        }
        return firstShown;
    }

    private SimpleArrayMap<NotificationTag, Collection<DownloadBatch>> clusterBatchesByNotificationTag(Collection<DownloadBatch> batches) {
        SimpleArrayMap<NotificationTag, Collection<DownloadBatch>> taggedBatches = new SimpleArrayMap<>();

        for (DownloadBatch batch : batches) {
            NotificationTag tag = NotificationTag.create(batch, context.getPackageName());
            associateBatchWithTag(tag, batch, taggedBatches);
        }

        return taggedBatches;
    }

    private void associateBatchWithTag(NotificationTag tag, DownloadBatch batch, SimpleArrayMap<NotificationTag, Collection<DownloadBatch>> taggedBatches) {
        if (tag == null) {
            return;
        }

        if (taggedBatches.containsKey(tag)) {
            taggedBatches.get(tag).add(batch);
        } else {
            Collection<DownloadBatch> batchesForTag = new ArrayList<>();
            batchesForTag.add(batch);
            taggedBatches.put(tag, batchesForTag);
        }
    }

    private List<Integer> getStaleTagsThatWereNotRenewed(SimpleArrayMap<NotificationTag, Collection<DownloadBatch>> clusteredBatches) {
        List<Integer> staleTags = new ArrayList<>();

        for (int i = activeNotifications.size() - 1; i >= 0; i--) {
            NotificationTag tag = activeNotifications.keyAt(i);
            if (!clusteredBatches.containsKey(tag)) {
                staleTags.add(tag.hashCode());
                activeNotifications.removeAt(i);
            }
        }

        return staleTags;
    }

}
