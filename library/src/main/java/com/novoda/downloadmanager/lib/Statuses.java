package com.novoda.downloadmanager.lib;

import android.support.v4.util.SparseArrayCompat;

import java.util.Arrays;
import java.util.List;

class Statuses {

    private static final List<Integer> PRIORITISED_STATUSES = Arrays.asList(
            DownloadStatus.CANCELED,
            DownloadStatus.PAUSING,
            DownloadStatus.PAUSED_BY_APP,
            DownloadStatus.RUNNING,
            DownloadStatus.DELETING,

            // Paused statuses
            DownloadStatus.QUEUED_DUE_CLIENT_RESTRICTIONS,
            DownloadStatus.WAITING_TO_RETRY,
            DownloadStatus.WAITING_FOR_NETWORK,
            DownloadStatus.QUEUED_FOR_WIFI,

            DownloadStatus.SUBMITTED,
            DownloadStatus.PENDING,
            DownloadStatus.SUCCESS
    );

    private static final List<Integer> STATUSES_EXCEPT_SUCCESS_SUBMITTED = Arrays.asList(
            DownloadStatus.CANCELED,
            DownloadStatus.PAUSED_BY_APP,
            DownloadStatus.RUNNING,
            DownloadStatus.DELETING,

            // Paused statuses
            DownloadStatus.QUEUED_DUE_CLIENT_RESTRICTIONS,
            DownloadStatus.WAITING_TO_RETRY,
            DownloadStatus.WAITING_FOR_NETWORK,
            DownloadStatus.QUEUED_FOR_WIFI,

            DownloadStatus.PENDING
    );

    private static final int NO_ERROR_STATUS = 0;

    private final SparseArrayCompat<Integer> statusCounts = new SparseArrayCompat<>(PRIORITISED_STATUSES.size());
    private int firstErrorStatus = NO_ERROR_STATUS;

    boolean hasNoItemsWithStatuses(List<Integer> excludedStatuses) {
        for (int status : excludedStatuses) {
            if (hasCountFor(status)) {
                return false;
            }
        }

        return true;
    }

    boolean hasCountFor(int statusCode) {
        return statusCounts.get(statusCode, 0) > 0;
    }

    void incrementCountFor(int statusCode) {
        if (DownloadStatus.isError(statusCode) && !hasErrorStatus()) {
            firstErrorStatus = statusCode;
        }

        int currentStatusCount = statusCounts.get(statusCode, 0);
        statusCounts.put(statusCode, currentStatusCount + 1);
    }

    void clear() {
        statusCounts.clear();
        firstErrorStatus = NO_ERROR_STATUS;
    }

    boolean hasOnlyCompleteAndSubmittedStatuses() {
        boolean hasCompleteItems = hasCountFor(DownloadStatus.SUCCESS);
        boolean hasSubmittedItems = hasCountFor(DownloadStatus.SUBMITTED);
        boolean hasNotOtherItems = hasNoItemsWithStatuses(STATUSES_EXCEPT_SUCCESS_SUBMITTED);

        return hasCompleteItems && hasSubmittedItems && hasNotOtherItems;
    }

    boolean hasErrorStatus() {
        return firstErrorStatus != NO_ERROR_STATUS;
    }

    int getFirstErrorStatus() {
        return firstErrorStatus;
    }

    int getFirstStatusByPriority() {
        for (int status : PRIORITISED_STATUSES) {
            if (hasCountFor(status)) {
                return status;
            }
        }

        return DownloadStatus.UNKNOWN_ERROR;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("{");

        int size = statusCounts.size();
        for (int i = 0; i < size; i++) {
            stringBuilder
                    .append("[status: ")
                    .append(statusCounts.keyAt(i))
                    .append(", count: ")
                    .append(statusCounts.valueAt(i))
                    .append("]");
        }

        return stringBuilder.append("}").toString();
    }
}
