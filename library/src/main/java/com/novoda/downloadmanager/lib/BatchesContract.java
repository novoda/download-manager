package com.novoda.downloadmanager.lib;

import android.provider.BaseColumns;

/**
 * Constants related to batches associated with each download.
 */
final class BatchesContract implements BaseColumns {

    public static final String BATCHES_TABLE_NAME = "batches";

    /**
     * The name of the column where the initiating application can provided the
     * title of this batch. The title will be displayed ito the user in the
     * list of batches.
     * <P>Type: TEXT</P>
     * <P>Owner can Init/Read/Write</P>
     */
    public static final String COLUMN_TITLE = "batch_title";

    /**
     * The name of the column where the initiating application can provide the
     * description of this batch. The description will be displayed to the
     * user in the list of batches.
     * <P>Type: TEXT</P>
     * <P>Owner can Init/Read/Write</P>
     */
    public static final String COLUMN_DESCRIPTION = "batch_description";

    /**
     * A URL that will be used to show a big picture style notification
     */
    public static final String COLUMN_BIG_PICTURE = "batch_notificationBigPictureResourceId";

    /**
     * The status of the batch.
     * <P>Type: INTEGER</P>
     * <P>Owner can Read</P>
     */
    public static final String COLUMN_STATUS = "batch_status";

    /**
     * The name of the column containing the flags that controls whether the
     * batch is displayed by the UI. See the {@link NotificationVisibility} constants for
     * a list of legal values.
     * <P>Type: INTEGER</P>
     * <P>Owner can Init/Read/Write</P>
     */
    public static final String COLUMN_VISIBILITY = "visibility";

    /**
     * Set to true if this batch is deleted. Its downloads will also be deleted.
     * <P>Type: BOOLEAN</P>
     * <P>Owner can Read</P>
     */
    public static final String COLUMN_DELETED = "deleted";

    /**
     * The total size of the batch in bytes.
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_TOTAL_BYTES = "batch_total_bytes";

    /**
     * The current size of the batch in bytes (on device).
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_CURRENT_BYTES = "batch_current_bytes";

    private BatchesContract() {
        // non-instantiable class
    }
}
