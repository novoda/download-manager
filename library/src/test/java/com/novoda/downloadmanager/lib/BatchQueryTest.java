package com.novoda.downloadmanager.lib;

import java.util.Arrays;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class BatchQueryTest {

    @Test
    public void givenNoConstraintsWhenTheQueryIsBuiltItHasNullSelectionAndNullSelectionArguments() {
        BatchQuery query = new BatchQuery.Builder().build();

        assertThat(query.getSelection()).isEmpty();
        assertThat(query.getSelectionArguments()).isEmpty();
    }

    @Test
    public void givenTheQueryIsWithIdWhenItIsBuiltThenTheSelectionAndArgumentsAreCorrect() {
        int id = 12;
        BatchQuery query = new BatchQuery.Builder().withId(id).build();

        assertThat(query.getSelection()).isEqualTo("(" + DownloadContract.Batches._ID + "=?)");
        assertThatSelectionArgumentAreEqualTo(query.getSelectionArguments(), new Integer[]{id});
    }

    @Test
    public void givenTheQueryIsWithExtraDataWhenItIsBuiltThenTheSelectionAndArgumentsAreCorrect() {
        String extraData = "extraData";
        BatchQuery query = new BatchQuery.Builder().withExtraData(extraData).build();

        assertThat(query.getSelection()).isEqualTo("(" + DownloadContract.Batches.COLUMN_EXTRA_DATA + "=?)");
        assertThatSelectionArgumentAreEqualTo(query.getSelectionArguments(), new String[]{extraData});
    }

    @Test
    public void givenTheQueryIsWithPendingStatusFilterWhenItIsBuiltThenTheSelectionAndArgumentsAreCorrect() {
        BatchQuery query = new BatchQuery.Builder().withStatusFilter(DownloadManager.STATUS_PENDING).build();

        assertThat(query.getSelection()).isEqualTo("(" + DownloadContract.Batches.COLUMN_STATUS + "=?)");
        assertThatSelectionArgumentAreEqualTo(query.getSelectionArguments(), new Integer[]{DownloadStatus.PENDING});
    }

    @Test
    public void givenTheQueryIsWithRunningStatusFilterWhenItIsBuiltThenTheSelectionAndArgumentsAreCorrect() {
        BatchQuery query = new BatchQuery.Builder().withStatusFilter(DownloadManager.STATUS_RUNNING).build();

        assertThat(query.getSelection()).isEqualTo("(" + DownloadContract.Batches.COLUMN_STATUS + "=?)");
        assertThatSelectionArgumentAreEqualTo(query.getSelectionArguments(), new Integer[]{DownloadStatus.RUNNING});
    }

    @Test
    public void givenTheQueryIsWithDeletingStatusFilterWhenItIsBuiltThenTheSelectionAndArgumentsAreCorrect() {
        BatchQuery query = new BatchQuery.Builder().withStatusFilter(DownloadManager.STATUS_DELETING).build();

        assertThat(query.getSelection()).isEqualTo("(" + DownloadContract.Batches.COLUMN_STATUS + "=?)");
        assertThatSelectionArgumentAreEqualTo(query.getSelectionArguments(), new Integer[]{DownloadStatus.DELETING});
    }

    @Test
    public void givenTheQueryIsWithPausedStatusFilterWhenItIsBuiltThenTheSelectionAndArgumentsAreCorrect() {
        BatchQuery query = new BatchQuery.Builder().withStatusFilter(DownloadManager.STATUS_PAUSED).build();

        assertThat(query.getSelection()).isEqualTo(
                "(" + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=?)");
        Integer[] expectedArguments = {
                DownloadStatus.PAUSED_BY_APP,
                DownloadStatus.WAITING_TO_RETRY,
                DownloadStatus.WAITING_FOR_NETWORK,
                DownloadStatus.QUEUED_FOR_WIFI
        };
        assertThatSelectionArgumentAreEqualTo(query.getSelectionArguments(), expectedArguments);
    }

    @Test
    public void givenTheQueryIsWithFailedStatusFilterWhenItIsBuiltThenTheSelectionAndArgumentsAreCorrect() {
        BatchQuery query = new BatchQuery.Builder().withStatusFilter(DownloadManager.STATUS_FAILED).build();

        assertThat(query.getSelection()).isEqualTo(
                "((" + DownloadContract.Batches.COLUMN_STATUS + ">=? AND "
                        + DownloadContract.Batches.COLUMN_STATUS + "<?))");
        Integer[] expectedArguments = {400, 600};
        assertThatSelectionArgumentAreEqualTo(query.getSelectionArguments(), expectedArguments);
    }

    @Test
    public void givenTheQueryIsWithTwoMultipleStatusFilterWhenItIsBuiltThenTheSelectionAndArgumentsAreCorrect() {
        BatchQuery query = new BatchQuery.Builder()
                .withStatusFilter(
                        DownloadManager.STATUS_PAUSED
                                | DownloadManager.STATUS_FAILED).build();

        assertThat(query.getSelection()).isEqualTo(
                "(" + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + "(" + DownloadContract.Batches.COLUMN_STATUS + ">=? AND "
                        + DownloadContract.Batches.COLUMN_STATUS + "<?))");
        Integer[] expectedArguments = {
                DownloadStatus.PAUSED_BY_APP,
                DownloadStatus.WAITING_TO_RETRY,
                DownloadStatus.WAITING_FOR_NETWORK,
                DownloadStatus.QUEUED_FOR_WIFI,
                400,
                600
        };
        assertThatSelectionArgumentAreEqualTo(query.getSelectionArguments(), expectedArguments);
    }

    @Test
    public void givenTheQueryIsWithThreeMultipleStatusFilterWhenItIsBuiltThenTheSelectionAndArgumentsAreCorrect() {
        BatchQuery query = new BatchQuery.Builder()
                .withStatusFilter(
                        DownloadManager.STATUS_PENDING
                                | DownloadManager.STATUS_RUNNING
                                | DownloadManager.STATUS_PAUSED)
                .build();

        assertThat(query.getSelection()).isEqualTo(
                "(" + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=?)");
        Integer[] expectedArguments = {
                DownloadStatus.PENDING,
                DownloadStatus.RUNNING,
                DownloadStatus.PAUSED_BY_APP,
                DownloadStatus.WAITING_TO_RETRY,
                DownloadStatus.WAITING_FOR_NETWORK,
                DownloadStatus.QUEUED_FOR_WIFI
        };
        assertThatSelectionArgumentAreEqualTo(query.getSelectionArguments(), expectedArguments);

    }

    @Test
    public void givenTheQueryIsWithFourMultipleStatusFilterWhenItIsBuiltThenTheSelectionAndArgumentsAreCorrect() {
        BatchQuery query = new BatchQuery.Builder()
                .withStatusFilter(
                        DownloadManager.STATUS_RUNNING
                                | DownloadManager.STATUS_PENDING
                                | DownloadManager.STATUS_PAUSED
                                | DownloadManager.STATUS_SUCCESSFUL)
                .build();

        assertThat(query.getSelection()).isEqualTo(
                "(" + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=?)");

        Integer[] expectedArguments = {
                DownloadStatus.PENDING,
                DownloadStatus.RUNNING,
                DownloadStatus.PAUSED_BY_APP,
                DownloadStatus.WAITING_TO_RETRY,
                DownloadStatus.WAITING_FOR_NETWORK,
                DownloadStatus.QUEUED_FOR_WIFI,
                DownloadStatus.SUCCESS
        };
        assertThatSelectionArgumentAreEqualTo(query.getSelectionArguments(), expectedArguments);
    }

    @Test
    public void givenTheQueryIsWithAllMultipleStatusFilterWhenItIsBuiltThenTheSelectionAndArgumentsAreCorrect() {
        BatchQuery query = new BatchQuery.Builder()
                .withStatusFilter(
                        DownloadManager.STATUS_FAILED
                                | DownloadManager.STATUS_SUCCESSFUL
                                | DownloadManager.STATUS_RUNNING
                                | DownloadManager.STATUS_PENDING
                                | DownloadManager.STATUS_PAUSED
                                | DownloadManager.STATUS_DELETING)
                .build();

        assertThat(query.getSelection()).isEqualTo(
                "(" + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + "(" + DownloadContract.Batches.COLUMN_STATUS + ">=? AND "
                        + DownloadContract.Batches.COLUMN_STATUS + "<?))");

        Integer[] expectedArguments = {
                DownloadStatus.PENDING,
                DownloadStatus.RUNNING,
                DownloadStatus.PAUSED_BY_APP,
                DownloadStatus.WAITING_TO_RETRY,
                DownloadStatus.WAITING_FOR_NETWORK,
                DownloadStatus.QUEUED_FOR_WIFI,
                DownloadStatus.DELETING,
                DownloadStatus.SUCCESS,
                400,
                600,
        };
        assertThatSelectionArgumentAreEqualTo(query.getSelectionArguments(), expectedArguments);
    }

    @Test
    public void givenBothWithIdAndWithStatusFilterAreOnTheQueryWhenItIsBuiltThenTheSelectionAndArgumentsAreCorrect() {
        int id = 14;
        BatchQuery query = new BatchQuery.Builder()
                .withStatusFilter(
                        DownloadManager.STATUS_FAILED
                                | DownloadManager.STATUS_SUCCESSFUL
                                | DownloadManager.STATUS_RUNNING
                                | DownloadManager.STATUS_PENDING
                                | DownloadManager.STATUS_PAUSED
                                | DownloadManager.STATUS_DELETING)
                .withId(id)
                .build();

        assertThat(query.getSelection()).isEqualTo(
                "(" + DownloadContract.Batches._ID + "=?) AND "
                        + "(" + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + "(" + DownloadContract.Batches.COLUMN_STATUS + ">=? AND "
                        + DownloadContract.Batches.COLUMN_STATUS + "<?))");

        Integer[] expectedArguments = {
                id,
                DownloadStatus.PENDING,
                DownloadStatus.RUNNING,
                DownloadStatus.PAUSED_BY_APP,
                DownloadStatus.WAITING_TO_RETRY,
                DownloadStatus.WAITING_FOR_NETWORK,
                DownloadStatus.QUEUED_FOR_WIFI,
                DownloadStatus.DELETING,
                DownloadStatus.SUCCESS,
                400,
                600
        };
        assertThatSelectionArgumentAreEqualTo(query.getSelectionArguments(), expectedArguments);
    }

    @Test
    public void givenSettingStatusFilterMultipleTimesWhenTheQueryIsBuiltThenOnlyTheLastFilterIsTakenIntoConsideration() {
        BatchQuery query = new BatchQuery.Builder()
                .withStatusFilter(DownloadManager.STATUS_PENDING)
                .withStatusFilter(DownloadManager.STATUS_FAILED)
                .build();

        assertThat(query.getSelection()).isEqualTo(
                "((" + DownloadContract.Batches.COLUMN_STATUS + ">=? AND "
                        + DownloadContract.Batches.COLUMN_STATUS + "<?))");

        Integer[] expectedArguments = {
                400,
                600
        };
        assertThatSelectionArgumentAreEqualTo(query.getSelectionArguments(), expectedArguments);

    }

    @Test
    public void givenSettingIdFilterMultipleTimesWhenTheQueryIsBuiltThenOnlyTheLastFilterIsTakenIntoConsideration() {
        int firstId = 12;
        int secondId = 13;
        BatchQuery query = new BatchQuery.Builder()
                .withId(firstId)
                .withId(secondId)
                .build();

        assertThat(query.getSelection()).isEqualTo("(" + DownloadContract.Batches._ID + "=?)");
        Integer[] expectedArguments = {secondId};
        assertThatSelectionArgumentAreEqualTo(query.getSelectionArguments(), expectedArguments);

    }

    @Test
    public void givenMultipleSettingIdFilterAndStatusFilterMultipleTimesWhenTheQueryIsBuiltThenOnlyTheLastFilterFromBothIsTakenIntoConsideration() {
        int firstId = 12;
        int secondId = 13;
        int thirdId = 14;
        BatchQuery query = new BatchQuery.Builder()
                .withId(firstId)
                .withStatusFilter(DownloadManager.STATUS_PENDING)
                .withId(secondId)
                .withStatusFilter(DownloadManager.STATUS_PENDING
                        | DownloadManager.STATUS_RUNNING)
                .withId(thirdId)
                .build();

        assertThat(query.getSelection()).isEqualTo(
                "(" + DownloadContract.Batches._ID + "=?) AND "
                        + "(" + DownloadContract.Batches.COLUMN_STATUS + "=? OR "
                        + DownloadContract.Batches.COLUMN_STATUS + "=?)");
        Integer[] expectedArguments = {
                thirdId,
                DownloadStatus.PENDING,
                DownloadStatus.RUNNING};
        assertThatSelectionArgumentAreEqualTo(query.getSelectionArguments(), expectedArguments);

    }

    @Test
    public void givenAscendingSortOrderWhenTheQueryIsBuiltThenTheSortIsCorrectlyTakenIntoConsideration() {
        String sortColumn = "sort_column";
        BatchQuery query = new BatchQuery.Builder()
                .withStatusFilter(DownloadManager.STATUS_PENDING)
                .withSortAscendingBy(sortColumn)
                .build();

        assertThat(query.getSelection()).isEqualTo("(" + DownloadContract.Batches.COLUMN_STATUS + "=?)");
        assertThat(query.getSortOrder()).isEqualTo(sortColumn + " ASC ");
    }

    @Test
    public void givenDescendingSortOrderWhenTheQueryIsBuiltThenTheSortIsCorrectlyTakenIntoConsideration() {
        String sortColumn = "sort_column";
        BatchQuery query = new BatchQuery.Builder()
                .withStatusFilter(DownloadManager.STATUS_PENDING)
                .withSortDescendingBy(sortColumn)
                .build();

        assertThat(query.getSelection()).isEqualTo("(" + DownloadContract.Batches.COLUMN_STATUS + "=?)");
        assertThat(query.getSortOrder()).isEqualTo(sortColumn + " DESC ");
    }

    @Test
    public void givenSortingByLivelinessWhenTheQueryIsBuiltThenTheSortIsCorrectlyBuilt() {
        BatchQuery query = new BatchQuery.Builder().withSortByLiveness().build();

        assertThat(query.getSortOrder()).isEqualTo("CASE batch_status " +
                "WHEN 192 THEN 1 " +
                "WHEN 190 THEN 2 " +
                "WHEN 193 THEN 3 " +
                "WHEN 498 THEN 4 " +
                "WHEN 200 THEN 5 " +
                "ELSE 6 END, _id ASC");
    }

    private void assertThatSelectionArgumentAreEqualTo(Object[] firstArray, Object[] secondArray) {
        assertThat(Arrays.toString(firstArray)).isEqualTo(Arrays.toString(secondArray));
    }

}
