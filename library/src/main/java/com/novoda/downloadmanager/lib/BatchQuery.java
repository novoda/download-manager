package com.novoda.downloadmanager.lib;

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
        private static final long NO_FILTER_BY_ID = -1L;

        private Criteria.Builder criteriaIdBuilder;
        private Criteria.Builder criteriaStatusBuilder;
        private long id = NO_FILTER_BY_ID;
        private int[] statusFlag;

        public Builder() {
            this.criteriaStatusBuilder = new Criteria.Builder();
            this.criteriaIdBuilder = new Criteria.Builder();
        }

        public Builder withId(long id) {
            this.id = id;
            criteriaIdBuilder
                    .withSelection(Downloads.Impl.Batches._ID, Criteria.Wildcard.EQUALS)
                    .withArgument(String.valueOf(id));
            return this;
        }

        public Builder withStatusFilter(int... statuses) {
            this.statusFlag = statuses;
            for (int i = 0; i < statuses.length; i++) {
                int status = statuses[i];
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

                if (isNotTheLastElement(i, statuses)) {
                    criteriaStatusBuilder.or();
                }
            }
            return this;
        }

        private boolean isNotTheLastElement(int index, int[] items) {
            return index != (items.length - 1);
        }

        public BatchQuery build() {
            Criteria.Builder builder = new Criteria.Builder();
            if (id == NO_FILTER_BY_ID && statusFlag == null) {
                return ALL;
            }

            if (id != NO_FILTER_BY_ID) {
                builder.withInnerCriteria(criteriaIdBuilder.build());
                if (statusFlag != null) {
                    builder.and();
                }
            }

            if (statusFlag != null) {
                builder.withInnerCriteria(criteriaStatusBuilder.build());
            }

            Criteria criteria = builder.build();
            String selection = criteria.getSelection();
            String[] selectionArguments = criteria.getSelectionArguments();
            return new BatchQuery(selection, selectionArguments);
        }
    }
}
