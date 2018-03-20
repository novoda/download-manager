package com.novoda.downloadmanager;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.novoda.downloadmanager.NetworkResponseFixtures.aNetworkResponse;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class NetworkFileSizeRequesterTest {

    private static final HttpClient.NetworkResponse UNSUCCESSFUL_RESPONSE = aNetworkResponse().withSuccessful(false).build();
    private static final HttpClient.NetworkResponse SUCCESSFUL_RESPONSE = aNetworkResponse().withHeader("1000").withSuccessful(true).build();
    private static final String ANY_RAW_URL = "http://example.com";
    private static final int FILE_BYTES = 1000;

    private final HttpClient httpClient = mock(HttpClient.class);
    private final NetworkRequestCreator requestCreator = new NetworkRequestCreator();

    private NetworkFileSizeRequester fileSizeRequester;

    @Before
    public void setUp() {
        fileSizeRequester = new NetworkFileSizeRequester(httpClient, requestCreator);
    }

    @Test
    public void returnsUnknownSize_whenHttpClientErrors() throws IOException {
        given(httpClient.execute(requestCreator.createFileSizeRequest(ANY_RAW_URL))).willThrow(IOException.class);

        FileSize fileSize = fileSizeRequester.requestFileSize(ANY_RAW_URL);

        assertThat(fileSize).isEqualTo(FileSizeCreator.unknownFileSize());
    }

    @Test
    public void returnsUnknownSize_whenResponseIsUnsuccessful() throws IOException {
        given(httpClient.execute(requestCreator.createFileSizeRequest(ANY_RAW_URL))).willReturn(UNSUCCESSFUL_RESPONSE);

        FileSize fileSize = fileSizeRequester.requestFileSize(ANY_RAW_URL);

        assertThat(fileSize).isEqualTo(FileSizeCreator.unknownFileSize());
    }

    @Test
    public void returnsFileSize_whenResponseSuccessful() throws IOException {
        given(httpClient.execute(requestCreator.createFileSizeRequest(ANY_RAW_URL))).willReturn(SUCCESSFUL_RESPONSE);

        FileSize fileSize = fileSizeRequester.requestFileSize(ANY_RAW_URL);

        assertThat(fileSize).isEqualTo(FileSizeCreator.createFromTotalSize(FILE_BYTES));
    }
}
