package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import static com.novoda.downloadmanager.lib.DownloadContract.Downloads.*;
import static com.novoda.downloadmanager.lib.DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP;
import static com.novoda.downloadmanager.lib.DownloadManager.COLUMN_STATUS;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class QueryTest {

    @Mock
    private Uri uri;

    @Mock
    private ContentResolver resolver;

    @Captor
    ArgumentCaptor<String> stringArgumentCaptor;

    private Query query;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        query = new Query();
    }

    @Test
    public void givenBatchIdsWhenTheQueryIsCreatedThenTheWhereStatementIsCorrect() {
        query.setFilterByBatchId(1, 2, 3).runQuery(resolver, null, uri);

        verify(resolver).query(any(Uri.class), any(String[].class), stringArgumentCaptor.capture(), any(String[].class), anyString());

        assertSelectionContains(COLUMN_BATCH_ID + " IN (1,2,3)");
    }

    @Test
    public void givenNoBatchIdsWhenTheQueryIsCreatedThenTheWhereStatementContainsNoBatchIdPredicate() {
        query.runQuery(resolver, null, uri);

        verify(resolver).query(any(Uri.class), any(String[].class), stringArgumentCaptor.capture(), any(String[].class), anyString());

        assertSelectionDoesNotContain(COLUMN_BATCH_ID + " IN ");
    }

    @Test
    public void whenWeSetABatchStatusOrderByOnAQueryThenTheResolverIsQueriedWithTheCorrectSortOrder() {
        query.orderBy(COLUMN_LAST_MODIFIED_TIMESTAMP, Query.ORDER_ASCENDING).runQuery(resolver, null, uri);

        // Actually resolves to DownloadContract.Downloads.COLUMN_LAST_MODIFIED
        verify(resolver).query(any(Uri.class), any(String[].class), anyString(), any(String[].class), eq("last_modified_timestamp ASC"));
    }

    @Test
    public void whenWeSetNoOrderByOnAQueryThenTheResolverIsQueriedWithTheLastModifiedSortOrder() {
        query.runQuery(resolver, null, uri);

        verify(resolver).query(any(Uri.class), any(String[].class), anyString(), any(String[].class), eq("last_modified_timestamp DESC"));
    }

    @Test
    public void whenOrderingByLivenessThenTheResolverIsQueriedWithTheExpectedSort() {
        query.orderByLiveness().runQuery(resolver, null, uri);

        verify(resolver).query(
                any(Uri.class), any(String[].class), anyString(), any(String[].class), eq(
                        "CASE status " +
                                "WHEN 192 THEN 1 " +
                                "WHEN 190 THEN 2 " +
                                "WHEN 193 THEN 3 " +
                                "WHEN 498 THEN 4 " +
                                "WHEN 200 THEN 5 " +
                                "ELSE 6 END, _id ASC"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenWeSetAnUnsupportedOrderByOnAQueryThenTheResolverIsQueriedWithTheCorrectSortOrder() {
        int anyOrder = Query.ORDER_ASCENDING;

        query.orderBy(COLUMN_STATUS, anyOrder).runQuery(resolver, null, uri);

        // Expecting an exception
    }

    @Test
    public void givenNotificationExtrasWhenTheQueryIsCreatedThenTheWhereStatementIsCorrect() {
        query.setFilterByNotificationExtras("something extra").runQuery(resolver, null, uri);

        verify(resolver).query(any(Uri.class), any(String[].class), stringArgumentCaptor.capture(), any(String[].class), anyString());

        assertSelectionContains(COLUMN_NOTIFICATION_EXTRAS + " = 'something extra'");
    }

    @Test
    public void givenNoNotificationExtrasWhenTheQueryIsCreatedThenTheWhereStatementContainsNoBatchIdPredicate() {
        query.runQuery(resolver, null, uri);

        verify(resolver).query(any(Uri.class), any(String[].class), stringArgumentCaptor.capture(), any(String[].class), anyString());

        assertSelectionDoesNotContain(COLUMN_NOTIFICATION_EXTRAS + " IN ");
    }

    @Test
    public void givenExtraDataWhenTheQueryIsCreatedThenTheWhereStatementIsCorrect() {
        query.setFilterByExtraData("something extra").runQuery(resolver, null, uri);

        verify(resolver).query(any(Uri.class), any(String[].class), stringArgumentCaptor.capture(), any(String[].class), anyString());

        assertSelectionContains(COLUMN_EXTRA_DATA + " = 'something extra'");
    }

    @Test
    public void givenNoExtraDataWhenTheQueryIsCreatedThenTheWhereStatementContainsNoBatchIdPredicate() {
        query.runQuery(resolver, null, uri);

        verify(resolver).query(any(Uri.class), any(String[].class), stringArgumentCaptor.capture(), any(String[].class), anyString());

        assertSelectionDoesNotContain(COLUMN_EXTRA_DATA + " IN ");
    }

    @Test
    public void givenMultipleNotificationExtrasWhenTheQueryIsCreatedThenTheWhereStatementIsCorrect() {
        query.setFilterByNotificationExtras("13", "14").runQuery(resolver, null, uri);

        verify(resolver).query(any(Uri.class), any(String[].class), stringArgumentCaptor.capture(), any(String[].class), anyString());

        assertSelectionContains("(" + COLUMN_NOTIFICATION_EXTRAS + " = '13' OR " + COLUMN_NOTIFICATION_EXTRAS + " = '14')");
    }

    @Test
    public void givenEmptyNotificationExtrasWhenTheQueryIsCreatedThenTheWhereStatementDoesNotContainExtrasPredicate() {
        String[] empty = {};
        query.setFilterByNotificationExtras(empty).runQuery(resolver, null, uri);

        verify(resolver).query(any(Uri.class), any(String[].class), stringArgumentCaptor.capture(), any(String[].class), anyString());

        assertSelectionDoesNotContain(COLUMN_NOTIFICATION_EXTRAS);
    }

    private void assertSelectionContains(String sequence) {
        assertThat(stringArgumentCaptor.getValue()).contains(sequence);
    }

    private void assertSelectionDoesNotContain(String sequence) {
        assertThat(stringArgumentCaptor.getValue()).doesNotContain(sequence);
    }
}
