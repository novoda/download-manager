package com.novoda.downloadmanager.lib;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used to query the batches table. Use {@link BatchQuery.Builder} in order to build it.
 */
public class BatchQuery {
    public static final BatchQuery ALL = new BatchQuery(null, null, null);
    private static final int LOW_END_FAILED_STATUS_CODE = 400;
    private static final int HIGH_END_FAILED_STATUS_CODE = 600;

    private final String selection;
    private final String sortOrder;
    private final String[] selectionArguments;

    BatchQuery(String selection, String[] selectionArguments, String sortOrder) {
        this.selection = selection;
        this.sortOrder = sortOrder;
        this.selectionArguments = selectionArguments;
    }

    String getSelection() {
        return selection;
    }

    String getSortOrder() {
        return sortOrder;
    }

    String[] getSelectionArguments() {
        return selectionArguments;
    }

    public static class Builder {

        private final Criteria.Builder builder;

        private static final String ORDER_BY_LIVENESS = "CASE " + DownloadContract.Batches.COLUMN_STATUS + " "
                + "WHEN " + DownloadStatus.RUNNING + " THEN 1 "
                + "WHEN " + DownloadStatus.PENDING + " THEN 2 "
                + "WHEN " + DownloadStatus.PAUSED_BY_APP + " THEN 3 "
                + "WHEN " + DownloadStatus.BATCH_FAILED + " THEN 4 "
                + "WHEN " + DownloadStatus.SUCCESS + " THEN 5 "
                + "ELSE 6 "
                + "END, " + DownloadContract.Batches._ID + " ASC";

        private Criteria.Builder criteriaIdBuilder;
        private Criteria.Builder criteriaStatusBuilder;
        private Criteria.Builder criteriaExtraDataBuilder;

        public Builder() {
            builder = new Criteria.Builder();
        }

        /**
         * Filter by batch id
         *
         * @param id id of the batch
         * @return {@link BatchQuery.Builder}
         */
        public Builder withId(long id) {
            this.criteriaIdBuilder = new Criteria.Builder();
            criteriaIdBuilder
                    .withSelection(DownloadContract.Batches._ID, Criteria.Wildcard.EQUALS)
                    .withArgument(String.valueOf(id));
            return this;
        }

        /**
         * Sorting in ascending order by sort column
         *
         * @param sortColumn sort column
         * @return {@link BatchQuery.Builder}
         */
        public Builder withSortAscendingBy(String sortColumn) {
            builder.sortBy(sortColumn).ascending();
            return this;
        }

        /**
         * Sorting in descending order by sort column
         *
         * @param sortColumn sort column
         * @return {@link BatchQuery.Builder}
         */
        public Builder withSortDescendingBy(String sortColumn) {
            builder.sortBy(sortColumn).descending();
            return this;
        }

        /**
         * Sorts batches according to the 'liveness' of the download, i.e. in the order:
         * Downloading, queued, other, paused, failed, completed
         *
         * @return {@link BatchQuery.Builder}
         */
        public Builder withSortByLiveness() {
            builder.sortBy(ORDER_BY_LIVENESS);
            return this;
        }

        /**
         * Filter by status
         *
         * @param statusFlags status flags that can be combined with "|"
         *                    one of {@link DownloadManager#STATUS_PAUSED},
         *                    {@link DownloadManager#STATUS_FAILED},
         *                    {@link DownloadManager#STATUS_PENDING},
         *                    {@link DownloadManager#STATUS_SUCCESSFUL},
         *                    {@link DownloadManager#STATUS_RUNNING}
         *                    {@link DownloadManager#STATUS_DELETING}
         *                    <p/>
         *                    e.g. withStatusFilter(DownloadManager.STATUS_FAILED | DownloadManager.STATUS_PENDING)
         * @return {@link BatchQuery.Builder}
         */
        public Builder withStatusFilter(@Status int statusFlags) {
            this.criteriaStatusBuilder = new Criteria.Builder()
                    .joinWithOr(buildCriteriaListFrom(statusFlags));
            return this;
        }

        /**
         * Filter by extra data.
         *
         * @return {@link BatchQuery.Builder}
         */
        public Builder withExtraData(String extraData) {
            this.criteriaExtraDataBuilder = new Criteria.Builder();
            criteriaExtraDataBuilder
                    .withSelection(DownloadContract.Batches.COLUMN_EXTRA_DATA, Criteria.Wildcard.EQUALS)
                    .withArgument(extraData);
            return this;
        }

        @NonNull
        private List<Criteria> buildCriteriaListFrom(@Status int statusFlags) {
            List<Criteria> criteriaList = new ArrayList<>();
            if ((statusFlags & DownloadManager.STATUS_PENDING) != 0) {
                Criteria pendingCriteria = new Criteria.Builder()
                        .withSelection(DownloadContract.Batches.COLUMN_STATUS, Criteria.Wildcard.EQUALS)
                        .withArgument(String.valueOf(DownloadStatus.PENDING))
                        .build();
                criteriaList.add(pendingCriteria);
            }

            if ((statusFlags & DownloadManager.STATUS_RUNNING) != 0) {
                Criteria runningCriteria = new Criteria.Builder()
                        .withSelection(DownloadContract.Batches.COLUMN_STATUS, Criteria.Wildcard.EQUALS)
                        .withArgument(String.valueOf(DownloadStatus.RUNNING))
                        .build();
                criteriaList.add(runningCriteria);
            }

            if ((statusFlags & DownloadManager.STATUS_PAUSED) != 0) {
                Criteria pausedCriteria = new Criteria.Builder()
                        .withSelection(DownloadContract.Batches.COLUMN_STATUS, Criteria.Wildcard.EQUALS)
                        .withArgument(String.valueOf(DownloadStatus.PAUSED_BY_APP))
                        .or()
                        .withSelection(DownloadContract.Batches.COLUMN_STATUS, Criteria.Wildcard.EQUALS)
                        .withArgument(String.valueOf(DownloadStatus.WAITING_TO_RETRY))
                        .or()
                        .withSelection(DownloadContract.Batches.COLUMN_STATUS, Criteria.Wildcard.EQUALS)
                        .withArgument(String.valueOf(DownloadStatus.WAITING_FOR_NETWORK))
                        .or()
                        .withSelection(DownloadContract.Batches.COLUMN_STATUS, Criteria.Wildcard.EQUALS)
                        .withArgument(String.valueOf(DownloadStatus.QUEUED_FOR_WIFI))
                        .build();
                criteriaList.add(pausedCriteria);
            }

            if ((statusFlags & DownloadManager.STATUS_DELETING) != 0) {
                Criteria deletingCriteria = new Criteria.Builder()
                        .withSelection(DownloadContract.Batches.COLUMN_STATUS, Criteria.Wildcard.EQUALS)
                        .withArgument(String.valueOf(DownloadStatus.DELETING))
                        .build();
                criteriaList.add(deletingCriteria);
            }

            if ((statusFlags & DownloadManager.STATUS_SUCCESSFUL) != 0) {
                Criteria successfulCriteria = new Criteria.Builder()
                        .withSelection(DownloadContract.Batches.COLUMN_STATUS, Criteria.Wildcard.EQUALS)
                        .withArgument(String.valueOf(DownloadStatus.SUCCESS))
                        .build();
                criteriaList.add(successfulCriteria);

            }
            if ((statusFlags & DownloadManager.STATUS_FAILED) != 0) {
                Criteria failedCriteria = new Criteria.Builder()
                        .withInnerCriteria(
                                new Criteria.Builder()
                                        .withSelection(DownloadContract.Batches.COLUMN_STATUS, Criteria.Wildcard.MORE_THAN_EQUAL)
                                        .withArgument(String.valueOf(LOW_END_FAILED_STATUS_CODE))
                                        .and()
                                        .withSelection(DownloadContract.Batches.COLUMN_STATUS, Criteria.Wildcard.LESS_THAN)
                                        .withArgument(String.valueOf(HIGH_END_FAILED_STATUS_CODE))
                                        .build())
                        .build();
                criteriaList.add(failedCriteria);
            }
            return criteriaList;
        }

        public BatchQuery build() {
            if (criteriaIdBuilder != null) {
                builder.withInnerCriteria(criteriaIdBuilder.build());
                if (criteriaStatusBuilder != null) {
                    builder.and();
                }
            }

            if (criteriaStatusBuilder != null) {
                builder.withInnerCriteria(criteriaStatusBuilder.build());
                if (criteriaExtraDataBuilder != null) {
                    builder.and();
                }
            }

            if (criteriaExtraDataBuilder != null) {
                builder.withInnerCriteria(criteriaExtraDataBuilder.build());
            }

            Criteria criteria = builder.build();
            String selection = criteria.getSelection();
            String sortOrder = criteria.getSort();
            String[] selectionArguments = criteria.getSelectionArguments();
            return new BatchQuery(selection, selectionArguments, sortOrder);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(flag = true,
            value = {DownloadManager.STATUS_PENDING,
                    DownloadManager.STATUS_RUNNING,
                    DownloadManager.STATUS_PAUSED,
                    DownloadManager.STATUS_SUCCESSFUL,
                    DownloadManager.STATUS_FAILED,
                    DownloadManager.STATUS_DELETING})
    public @interface Status {
        //marker interface that ensures the annotated fields are in within the above values
    }
}
