package com.novoda.downloadmanager.lib;

import java.util.Arrays;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class BatchQueryTest {

    @Test
    public void givenNoConstraintsWhenTheQueryIsBuiltItHasNullSelectionAndNullSelectionArguments() {
        BatchQuery query = new BatchQuery.Builder().build();

        assertThat(query.getSelection()).isNull();
        assertThat(query.getSelectionArguments()).isNull();
    }

    @Test
    public void givenTheQueryIsWithIdWhenItIsBuiltThenTheSelectionAndArgumentsAreCorrect() {
        int id = 12;
        BatchQuery query = new BatchQuery.Builder().withId(id).build();

        assertThat(query.getSelection()).isEqualTo("(" + Downloads.Impl.Batches._ID + "=?)");
        assertThatTwoArraysAreEqual(query.getSelectionArguments(), new Integer[]{id});
    }

    @Test
    public void givenTheQueryIsWithPendingStatusFilterWhenItIsBuiltThenTheSelectionAndArgumentsAreCorrect() {
        BatchQuery query = new BatchQuery.Builder().withStatusFilter(DownloadManager.STATUS_PENDING).build();

        assertThat(query.getSelection()).isEqualTo("(" + Downloads.Impl.Batches.COLUMN_STATUS + "=?)");
        assertThatTwoArraysAreEqual(query.getSelectionArguments(), new Integer[]{Downloads.Impl.STATUS_PENDING});
    }

    @Test
    public void givenTheQueryIsWithRunningStatusFilterWhenItIsBuiltThenTheSelectionAndArgumentsAreCorrect() {
        BatchQuery query = new BatchQuery.Builder().withStatusFilter(DownloadManager.STATUS_RUNNING).build();

        assertThat(query.getSelection()).isEqualTo("(" + Downloads.Impl.Batches.COLUMN_STATUS + "=?)");
        assertThatTwoArraysAreEqual(query.getSelectionArguments(), new Integer[]{Downloads.Impl.STATUS_RUNNING});
    }

    @Test
    public void givenTheQueryIsWithPausedStatusFilterWhenItIsBuiltThenTheSelectionAndArgumentsAreCorrect() {
        BatchQuery query = new BatchQuery.Builder().withStatusFilter(DownloadManager.STATUS_PAUSED).build();

        assertThat(query.getSelection()).isEqualTo(
                "(" + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=?)");
        Integer[] expectedArguments = {
                Downloads.Impl.STATUS_PAUSED_BY_APP,
                Downloads.Impl.STATUS_WAITING_TO_RETRY,
                Downloads.Impl.STATUS_WAITING_FOR_NETWORK,
                Downloads.Impl.STATUS_QUEUED_FOR_WIFI
        };
        assertThatTwoArraysAreEqual(query.getSelectionArguments(), expectedArguments);
    }

    @Test
    public void givenTheQueryIsWithFailedStatusFilterWhenItIsBuiltThenTheSelectionAndArgumentsAreCorrect() {
        BatchQuery query = new BatchQuery.Builder().withStatusFilter(DownloadManager.STATUS_FAILED).build();

        assertThat(query.getSelection()).isEqualTo(
                "((" + Downloads.Impl.Batches.COLUMN_STATUS + ">=? AND "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "<?))");
        Integer[] expectedArguments = {400, 600};
        assertThatTwoArraysAreEqual(query.getSelectionArguments(), expectedArguments);
    }

    @Test
    public void givenTheQueryIsWithTwoMultipleStatusFilterWhenItIsBuiltThenTheSelectionAndArgumentsAreCorrect() {
        BatchQuery query = new BatchQuery.Builder().withStatusFilter(DownloadManager.STATUS_PAUSED, DownloadManager.STATUS_FAILED).build();

        assertThat(query.getSelection()).isEqualTo(
                "(" + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + "(" + Downloads.Impl.Batches.COLUMN_STATUS + ">=? AND "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "<?))");
        Integer[] expectedArguments = {
                Downloads.Impl.STATUS_PAUSED_BY_APP,
                Downloads.Impl.STATUS_WAITING_TO_RETRY,
                Downloads.Impl.STATUS_WAITING_FOR_NETWORK,
                Downloads.Impl.STATUS_QUEUED_FOR_WIFI,
                400,
                600
        };
        assertThatTwoArraysAreEqual(query.getSelectionArguments(), expectedArguments);
    }

    @Test
    public void givenTheQueryIsWithThreeMultipleStatusFilterWhenItIsBuiltThenTheSelectionAndArgumentsAreCorrect() {
        BatchQuery query = new BatchQuery.Builder().withStatusFilter(DownloadManager.STATUS_PENDING, DownloadManager.STATUS_RUNNING,
                DownloadManager.STATUS_PAUSED).build();

        assertThat(query.getSelection()).isEqualTo(
                "(" + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=?)");
        Integer[] expectedArguments = {
                Downloads.Impl.STATUS_PENDING,
                Downloads.Impl.STATUS_RUNNING,
                Downloads.Impl.STATUS_PAUSED_BY_APP,
                Downloads.Impl.STATUS_WAITING_TO_RETRY,
                Downloads.Impl.STATUS_WAITING_FOR_NETWORK,
                Downloads.Impl.STATUS_QUEUED_FOR_WIFI
        };
        assertThatTwoArraysAreEqual(query.getSelectionArguments(), expectedArguments);

    }

    @Test
    public void givenTheQueryIsWithFourMultipleStatusFilterWhenItIsBuiltThenTheSelectionAndArgumentsAreCorrect() {
        BatchQuery query = new BatchQuery.Builder().withStatusFilter(DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PENDING,
                DownloadManager.STATUS_PAUSED, DownloadManager.STATUS_SUCCESSFUL).build();

        assertThat(query.getSelection()).isEqualTo(
                "(" + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=?)");

        Integer[] expectedArguments = {
                Downloads.Impl.STATUS_RUNNING,
                Downloads.Impl.STATUS_PENDING,
                Downloads.Impl.STATUS_PAUSED_BY_APP,
                Downloads.Impl.STATUS_WAITING_TO_RETRY,
                Downloads.Impl.STATUS_WAITING_FOR_NETWORK,
                Downloads.Impl.STATUS_QUEUED_FOR_WIFI,
                Downloads.Impl.STATUS_SUCCESS
        };
        assertThatTwoArraysAreEqual(query.getSelectionArguments(), expectedArguments);
    }

    @Test
    public void givenTheQueryIsWithAllMultipleStatusFilterWhenItIsBuiltThenTheSelectionAndArgumentsAreCorrect() {
        BatchQuery query = new BatchQuery.Builder().withStatusFilter(DownloadManager.STATUS_FAILED, DownloadManager.STATUS_SUCCESSFUL,
                DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PENDING, DownloadManager.STATUS_PAUSED).build();

        assertThat(query.getSelection()).isEqualTo(
                "(" + "(" + Downloads.Impl.Batches.COLUMN_STATUS + ">=? AND "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "<?) OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=?)");

        Integer[] expectedArguments = {
                400,
                600,
                Downloads.Impl.STATUS_SUCCESS,
                Downloads.Impl.STATUS_RUNNING,
                Downloads.Impl.STATUS_PENDING,
                Downloads.Impl.STATUS_PAUSED_BY_APP,
                Downloads.Impl.STATUS_WAITING_TO_RETRY,
                Downloads.Impl.STATUS_WAITING_FOR_NETWORK,
                Downloads.Impl.STATUS_QUEUED_FOR_WIFI
        };
        assertThatTwoArraysAreEqual(query.getSelectionArguments(), expectedArguments);
    }

    @Test
    public void givenBothWithIdAndWithStatusFilterAreOnTheQueryWhenItIsBuiltThenTheSelectionAndArgumentsAreCorrect() {
        int id = 14;
        BatchQuery query = new BatchQuery.Builder()
                .withStatusFilter(
                        DownloadManager.STATUS_FAILED,
                        DownloadManager.STATUS_SUCCESSFUL,
                        DownloadManager.STATUS_RUNNING,
                        DownloadManager.STATUS_PENDING,
                        DownloadManager.STATUS_PAUSED)
                .withId(id)
                .build();

        assertThat(query.getSelection()).isEqualTo(
                "(" + Downloads.Impl.Batches._ID + "=?) AND "
                        + "(" + "(" + Downloads.Impl.Batches.COLUMN_STATUS + ">=? AND "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "<?) OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=? OR "
                        + Downloads.Impl.Batches.COLUMN_STATUS + "=?)");

        Integer[] expectedArguments = {
                id,
                400,
                600,
                Downloads.Impl.STATUS_SUCCESS,
                Downloads.Impl.STATUS_RUNNING,
                Downloads.Impl.STATUS_PENDING,
                Downloads.Impl.STATUS_PAUSED_BY_APP,
                Downloads.Impl.STATUS_WAITING_TO_RETRY,
                Downloads.Impl.STATUS_WAITING_FOR_NETWORK,
                Downloads.Impl.STATUS_QUEUED_FOR_WIFI
        };
        assertThatTwoArraysAreEqual(query.getSelectionArguments(), expectedArguments);
    }

    private void assertThatTwoArraysAreEqual(Object[] firstArray, Object[] secondArray) {
        assertThat(Arrays.toString(firstArray)).isEqualTo(Arrays.toString(secondArray));
    }

}
