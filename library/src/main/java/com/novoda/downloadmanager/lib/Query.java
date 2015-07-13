package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class may be used to filter download manager queries.
 */
public class Query {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ORDER_ASCENDING, ORDER_DESCENDING})
    public @interface Order {
    }

    /**
     * Constant for use with {@link #orderBy}
     */
    public static final int ORDER_ASCENDING = 1;

    /**
     * Constant for use with {@link #orderBy}
     */
    public static final int ORDER_DESCENDING = 2;

    private static final String ORDER_BY_LIVENESS = String.format("CASE %1$s "
                    + "WHEN %2$d THEN 1 "
                    + "WHEN %3$d THEN 2 "
                    + "WHEN %4$d THEN 3 "
                    + "WHEN %5$d THEN 4 "
                    + "WHEN %6$d THEN 5 "
                    + "ELSE 6 "
                    + "END, _id ASC",
            DownloadContract.Downloads.COLUMN_STATUS,
            DownloadStatus.RUNNING,
            DownloadStatus.PENDING,
            DownloadStatus.PAUSED_BY_APP,
            DownloadStatus.BATCH_FAILED,
            DownloadStatus.SUCCESS
    );

    private long[] downloadIds = null;
    private long[] batchIds = null;
    private Integer statusFlags = null;
    private boolean onlyIncludeVisibleInDownloadsUi = false;
    private String[] filterExtras;
    private String orderString = DownloadContract.Downloads.COLUMN_LAST_MODIFICATION + " DESC";

    /**
     * Include only the downloads with the given IDs.
     *
     * @return this object
     */
    public Query setFilterById(long... downloadIds) {
        this.downloadIds = downloadIds;
        return this;
    }

    /**
     * Include only the downloads with the given batch IDs.
     *
     * @return this object
     */
    public Query setFilterByBatchId(long... batchIds) {
        this.batchIds = batchIds;
        return this;
    }

    /**
     * Include only the downloads with the given extras.
     *
     * @return this object
     */
    public Query setFilterByExtras(String... extras) {
        filterExtras = extras;
        return this;
    }

    /**
     * Include only downloads with status matching any the given status flags.
     *
     * @param flags any combination of the STATUS_* bit flags
     * @return this object
     */
    public Query setFilterByStatus(int flags) {
        statusFlags = flags;
        return this;
    }

    /**
     * Controls whether this query includes downloads not visible in the system's Downloads UI.
     *
     * @param value if true, this query will only include downloads that should be displayed in
     *              the system's Downloads UI; if false (the default), this query will include
     *              both visible and invisible downloads.
     * @return this object
     */
    public Query setOnlyIncludeVisibleInDownloadsUi(boolean value) {
        onlyIncludeVisibleInDownloadsUi = value;
        return this;
    }

    /**
     * Change the sort order of the returned Cursor.
     *
     * @param column    one of the COLUMN_* constants; currently, only
     *                  {DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP} and {DownloadManager.COLUMN_TOTAL_SIZE_BYTES} are
     *                  supported.
     * @param direction either {@link #ORDER_ASCENDING} or {@link #ORDER_DESCENDING}
     * @return this object
     */
    public Query orderBy(String column, @Order int direction) {
        if (direction != ORDER_ASCENDING && direction != ORDER_DESCENDING) {
            throw new IllegalArgumentException("Invalid direction: " + direction);
        }

        String resolvedOrderColumn;
        switch (column) {
            case DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP:
                resolvedOrderColumn = DownloadContract.Downloads.COLUMN_LAST_MODIFICATION;
                break;
            case DownloadManager.COLUMN_TOTAL_SIZE_BYTES:
                resolvedOrderColumn = DownloadContract.Downloads.COLUMN_TOTAL_BYTES;
                break;
            default:
                throw new IllegalArgumentException("Cannot order by " + column);
        }

        String orderDirection = (direction == ORDER_ASCENDING ? "ASC" : "DESC");
        orderString = resolvedOrderColumn + " " + orderDirection;
        return this;
    }

    /**
     * Sorts downloads according to the 'liveness' of the download, i.e. in the order:
     * Downloading, queued, other, paused, failed, completed
     *
     * @return this {@link Query}
     */
    public Query orderByLiveness() {
        orderString = ORDER_BY_LIVENESS;
        return this;
    }

    /**
     * Run this query using the given ContentResolver.
     *
     * @param projection the projection to pass to ContentResolver.query()
     * @return the Cursor returned by ContentResolver.query()
     */
    Cursor runQuery(ContentResolver resolver, String[] projection, Uri baseUri) {
        List<String> selectionParts = new ArrayList<>();
        String[] selectionArgs = getIdsAsStringArray(downloadIds);

        filterByDownloadIds(selectionParts);
        filterByBatchIds(selectionParts);
        filterByExtras(selectionParts);
        filterByStatus(selectionParts);

        if (onlyIncludeVisibleInDownloadsUi) {
            selectionParts.add(DownloadContract.Downloads.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI + " != '0'");
        }

        // only return rows which are not marked 'deleted = 1'
//        selectionParts.add(DownloadContract.Downloads.COLUMN_DELETED + " != '1'");

        String selection = joinStrings(" AND ", selectionParts);

        return resolver.query(baseUri, null, null, null, orderString);
    }

    private String[] getIdsAsStringArray(long[] ids) {
        if (ids == null) {
            return null;
        }
        return longArrayToStringArray(ids);
    }

    private void filterByDownloadIds(List<String> selectionParts) {
        if (downloadIds == null) {
            return;
        }
        selectionParts.add(DownloadManager.getWhereClauseForIds(downloadIds));
    }

    private void filterByBatchIds(List<String> selectionParts) {
        if (batchIds == null || batchIds.length == 0) {
            return;
        }
        selectionParts.add(getWhereClauseForBatchIds(batchIds));
    }

    /**
     * Get a SQL WHERE clause to select a bunch of IDs.
     */
    private String getWhereClauseForBatchIds(long[] ids) {
        String[] idStrings = longArrayToStringArray(ids);
        return DownloadContract.Downloads.COLUMN_BATCH_ID + " IN (" + joinStrings(",", Arrays.asList(idStrings)) + ")";
    }

    private void filterByExtras(List<String> selectionParts) {
        if (filterExtras == null) {
            return;
        }
        List<String> parts = new ArrayList<>();
        for (String filterExtra : filterExtras) {
            parts.add(extrasClause(filterExtra));
        }
        selectionParts.add(joinStrings(" OR ", parts));
    }

    private void filterByStatus(List<String> selectionParts) {
        if (statusFlags == null) {
            return;
        }

        List<String> parts = new ArrayList<>();
        if ((statusFlags & DownloadManager.STATUS_PENDING) != 0) {
            parts.add(statusClause("=", DownloadStatus.PENDING));
        }
        if ((statusFlags & DownloadManager.STATUS_RUNNING) != 0) {
            parts.add(statusClause("=", DownloadStatus.RUNNING));
        }
        if ((statusFlags & DownloadManager.STATUS_PAUSED) != 0) {
            parts.add(statusClause("=", DownloadStatus.PAUSED_BY_APP));
            parts.add(statusClause("=", DownloadStatus.WAITING_TO_RETRY));
            parts.add(statusClause("=", DownloadStatus.WAITING_FOR_NETWORK));
            parts.add(statusClause("=", DownloadStatus.QUEUED_FOR_WIFI));
        }
        if ((statusFlags & DownloadManager.STATUS_SUCCESSFUL) != 0) {
            parts.add(statusClause("=", DownloadStatus.SUCCESS));
        }
        if ((statusFlags & DownloadManager.STATUS_FAILED) != 0) {
            parts.add("(" + statusClause(">=", 400) + " AND " + statusClause("<", 600) + ")");
        }
        selectionParts.add(joinStrings(" OR ", parts));
    }

    // copied from AOSP for testability
    private static String joinStrings(String joiner, Iterable<String> parts) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String part : parts) {
            if (!first) {
                builder.append(joiner);
            }
            builder.append(part);
            first = false;
        }
        return builder.toString();
    }

    private static String[] longArrayToStringArray(long[] longs) {
        String[] strings = new String[longs.length];
        for (int i = 0; i < longs.length; i++) {
            strings[i] = Long.toString(longs[i]);
        }
        return strings;
    }

    private String extrasClause(String extra) {
        return DownloadContract.Downloads.COLUMN_NOTIFICATION_EXTRAS + " = '" + extra + "'";
    }

    private String statusClause(String operator, int value) {
        return DownloadContract.Downloads.COLUMN_STATUS + operator + "'" + value + "'";
    }
}
