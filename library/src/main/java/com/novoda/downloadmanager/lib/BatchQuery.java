package com.novoda.downloadmanager.lib;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class BatchQuery {
    private static final BatchQuery ALL = new BatchQuery(null, null);

    private final String selection;
    private final String[] selectionArguments;

    BatchQuery(String selection, String[] selectionArguments) {
        this.selection = selection;
        this.selectionArguments = selectionArguments;
    }

    String getSelection() {
        return selection;
    }

    String[] getSelectionArguments() {
        return selectionArguments;
    }

    public static class Builder {

        private final Criteria.Builder builder;

        private Criteria.Builder criteriaIdBuilder;
        private Criteria.Builder criteriaStatusBuilder;

        public Builder() {
            builder = new Criteria.Builder();
        }

        public Builder withId(long id) {
            this.criteriaIdBuilder = new Criteria.Builder();
            criteriaIdBuilder
                    .withSelection(Downloads.Impl.Batches._ID, Criteria.Wildcard.EQUALS)
                    .withArgument(String.valueOf(id));
            return this;
        }

        public Builder withStatusFilter(@Status int statusFlags) {
            this.criteriaStatusBuilder = new Criteria.Builder();
            List<Integer> statuses = buildStatusesFrom(statusFlags);

            for (int status : statuses) {
                switch (status) {
                    case DownloadManager.STATUS_PENDING:
                        criteriaStatusBuilder
                                .withSelection(Downloads.Impl.Batches.COLUMN_STATUS, Criteria.Wildcard.EQUALS)
                                .withArgument(String.valueOf(Downloads.Impl.STATUS_PENDING));
                        break;
                    case DownloadManager.STATUS_RUNNING:
                        criteriaStatusBuilder
                                .withSelection(Downloads.Impl.Batches.COLUMN_STATUS, Criteria.Wildcard.EQUALS)
                                .withArgument(String.valueOf(Downloads.Impl.STATUS_RUNNING));
                        break;
                    case DownloadManager.STATUS_PAUSED:
                        criteriaStatusBuilder
                                .withSelection(Downloads.Impl.Batches.COLUMN_STATUS, Criteria.Wildcard.EQUALS)
                                .withArgument(String.valueOf(Downloads.Impl.STATUS_PAUSED_BY_APP))
                                .or()
                                .withSelection(Downloads.Impl.Batches.COLUMN_STATUS, Criteria.Wildcard.EQUALS)
                                .withArgument(String.valueOf(Downloads.Impl.STATUS_WAITING_TO_RETRY))
                                .or()
                                .withSelection(Downloads.Impl.Batches.COLUMN_STATUS, Criteria.Wildcard.EQUALS)
                                .withArgument(String.valueOf(Downloads.Impl.STATUS_WAITING_FOR_NETWORK))
                                .or()
                                .withSelection(Downloads.Impl.Batches.COLUMN_STATUS, Criteria.Wildcard.EQUALS)
                                .withArgument(String.valueOf(Downloads.Impl.STATUS_QUEUED_FOR_WIFI));
                        break;
                    case DownloadManager.STATUS_SUCCESSFUL:
                        criteriaStatusBuilder
                                .withSelection(Downloads.Impl.Batches.COLUMN_STATUS, Criteria.Wildcard.EQUALS)
                                .withArgument(String.valueOf(Downloads.Impl.STATUS_SUCCESS));
                        break;
                    case DownloadManager.STATUS_FAILED:
                        criteriaStatusBuilder
                                .withInnerCriteria(
                                        new Criteria.Builder()
                                                .withSelection(Downloads.Impl.Batches.COLUMN_STATUS, Criteria.Wildcard.MORE_THAN_EQUAL)
                                                .withArgument(String.valueOf(400))
                                                .and()
                                                .withSelection(Downloads.Impl.Batches.COLUMN_STATUS, Criteria.Wildcard.LESS_THAN)
                                                .withArgument(String.valueOf(600))
                                                .build());
                        break;
                    default:
                        break;

                }

                if (isNotLastIn(statuses, status)) {
                    criteriaStatusBuilder.or();
                }
            }
            return this;
        }

        @NonNull
        private List<Integer> buildStatusesFrom(@Status int statusFlags) {
            List<Integer> statuses = new ArrayList<>();
            if ((statusFlags & DownloadManager.STATUS_PENDING) != 0) {
                statuses.add(DownloadManager.STATUS_PENDING);
            }

            if ((statusFlags & DownloadManager.STATUS_RUNNING) != 0) {
                statuses.add(DownloadManager.STATUS_RUNNING);
            }

            if ((statusFlags & DownloadManager.STATUS_PAUSED) != 0) {
                statuses.add(DownloadManager.STATUS_PAUSED);
            }

            if ((statusFlags & DownloadManager.STATUS_SUCCESSFUL) != 0) {
                statuses.add(DownloadManager.STATUS_SUCCESSFUL);

            }
            if ((statusFlags & DownloadManager.STATUS_FAILED) != 0) {
                statuses.add(DownloadManager.STATUS_FAILED);
            }
            return statuses;
        }

        private boolean isNotLastIn(List<Integer> statuses, int status) {
            return statuses.lastIndexOf(status) != statuses.size() - 1;
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
            }

            Criteria criteria = builder.build();
            String selection = criteria.getSelection();
            String[] selectionArguments = criteria.getSelectionArguments();
            return new BatchQuery(selection, selectionArguments);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(flag = true,
            value = {DownloadManager.STATUS_PENDING,
                    DownloadManager.STATUS_RUNNING,
                    DownloadManager.STATUS_PAUSED,
                    DownloadManager.STATUS_SUCCESSFUL,
                    DownloadManager.STATUS_FAILED})
    public @interface Status {
        //marker interface that ensures the annotated fields are in within the above values
    }
}
