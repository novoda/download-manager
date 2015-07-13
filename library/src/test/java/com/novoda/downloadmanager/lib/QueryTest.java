package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

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

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void givenBatchIdsWhenTheQueryIsCreatedThenTheWhereStatementIsCorrect() {
        new Query().setFilterByBatchId(1, 2, 3).runQuery(resolver, null, uri);

        verify(resolver).query(any(Uri.class), any(String[].class), stringArgumentCaptor.capture(), any(String[].class), anyString());

        assertThat(stringArgumentCaptor.getValue()).contains(DownloadContract.Downloads.COLUMN_BATCH_ID + " IN (1,2,3)");
    }

    @Test
    public void givenNoBatchIdsWhenTheQueryIsCreatedThenTheWhereStatementContainsNoBatchIdPredicate() {
        new Query().runQuery(resolver, null, uri);

        verify(resolver).query(any(Uri.class), any(String[].class), stringArgumentCaptor.capture(), any(String[].class), anyString());

        assertThat(stringArgumentCaptor.getValue()).doesNotContain(DownloadContract.Downloads.COLUMN_BATCH_ID + " IN ");
    }

    @Test
    public void whenWeSetABatchStatusOrderByOnAQueryThenTheResolverIsQueriedWithTheCorrectSortOrder() {
        new Query().orderBy(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP, Query.ORDER_ASCENDING).runQuery(resolver, null, uri);

        // Actually resolves to Downloads.Impl.COLUMN_LAST_MODIFIED
        verify(resolver).query(any(Uri.class), any(String[].class), anyString(), any(String[].class), eq("lastmod ASC"));
    }

    @Test
    public void whenWeSetNoOrderByOnAQueryThenTheResolverIsQueriedWithTheLastModifiedSortOrder() {
        new Query().runQuery(resolver, null, uri);

        verify(resolver).query(any(Uri.class), any(String[].class), anyString(), any(String[].class), eq("lastmod DESC"));
    }

    @Test
    public void whenOrderingByLivenessThenTheResolverIsQueriedWithTheExpectedSort() {
        new Query().orderByLiveness().runQuery(resolver, null, uri);

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
        new Query().orderBy(DownloadManager.COLUMN_STATUS, anyOrder).runQuery(resolver, null, uri);
        // Expecting an exception
    }

    @Test
    public void givenExtraDataWhenTheQueryIsCreatedThenTheWhereStatementIsCorrect() {
        new Query().setFilterByExtraData("something extra").runQuery(resolver, null, uri);

        verify(resolver).query(any(Uri.class), any(String[].class), stringArgumentCaptor.capture(), any(String[].class), anyString());

        assertThat(stringArgumentCaptor.getValue()).contains(DownloadContract.Downloads.COLUMN_EXTRA_DATA + " = 'something extra'");
    }

    @Test
    public void givenNoExtraDataWhenTheQueryIsCreatedThenTheWhereStatementContainsNoBatchIdPredicate() {
        new Query().runQuery(resolver, null, uri);

        verify(resolver).query(any(Uri.class), any(String[].class), stringArgumentCaptor.capture(), any(String[].class), anyString());

        assertThat(stringArgumentCaptor.getValue()).doesNotContain(DownloadContract.Downloads.COLUMN_EXTRA_DATA + " IN ");
    }
}
