package com.novoda.downloadmanager;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static com.novoda.downloadmanager.InternalFileSizeFixtures.aFileSize;
import static com.novoda.downloadmanager.NetworkResponseFixtures.aNetworkResponse;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class NetworkFileDownloaderTest {

    private static final byte[] BYTES_TO_RECEIVE = "s".getBytes();
    private static final HttpClient.NetworkResponse RESPONSE_WITH_INPUT_STREAM = aNetworkResponse()
            .withInputStream(new ByteArrayInputStream(BYTES_TO_RECEIVE))
            .build();

    private static final String ANY_RAW_URL = "http://example.com";
    private static final FileSize KNOWN_FILE_SIZE = aFileSize().withAreBytesDownloadedKnown(true).build();
    private static final FileSize UNKNOWN_FILE_SIZE = aFileSize().withAreBytesDownloadedKnown(false).build();
    private static final HttpClient.NetworkResponse INVALID_RESPONSE = aNetworkResponse().withCode(418).build();

    private final FileDownloader.Callback callback = mock(FileDownloader.Callback.class);
    private final HttpClient httpClient = mock(HttpClient.class);
    private final NetworkRequestCreator requestCreator = new NetworkRequestCreator();

    private NetworkFileDownloader networkFileDownloader;

    @Before
    public void setUp() {
        networkFileDownloader = new NetworkFileDownloader(httpClient, requestCreator);
    }

    @Test
    public void emitsBytes_whenDownloading() throws IOException {
        NetworkRequest downloadRequest = requestCreator.createDownloadRequestWithDownloadedBytesHeader(ANY_RAW_URL, KNOWN_FILE_SIZE.currentSize(), KNOWN_FILE_SIZE.totalSize());
        given(httpClient.execute(downloadRequest)).willReturn(RESPONSE_WITH_INPUT_STREAM);
        networkFileDownloader.startDownloading(ANY_RAW_URL, KNOWN_FILE_SIZE, callback);

        byte[] expectedBytes = new byte[4096];
        expectedBytes[0] = "s".getBytes()[0];

        verify(callback).onBytesRead(expectedBytes, 1);
    }

    @Test
    public void closesByteStream_whenDownloaded() throws IOException {
        HttpClient.NetworkResponse networkResponse = spy(aNetworkResponse().build());
        given(httpClient.execute(requestCreator.createDownloadRequest(ANY_RAW_URL))).willReturn(networkResponse);

        networkFileDownloader.startDownloading(ANY_RAW_URL, UNKNOWN_FILE_SIZE, callback);

        verify(networkResponse).closeByteStream();
    }

    @Test
    public void emitsError_whenResponseCodeIsInvalid() throws IOException {
        given(httpClient.execute(requestCreator.createDownloadRequest(ANY_RAW_URL))).willReturn(INVALID_RESPONSE);

        networkFileDownloader.startDownloading(ANY_RAW_URL, UNKNOWN_FILE_SIZE, callback);

        verify(callback).onError();
    }

    @Test
    public void emitsDownloadFinished_whenResponseCodeIsInvalid() throws IOException {
        given(httpClient.execute(requestCreator.createDownloadRequest(ANY_RAW_URL))).willReturn(INVALID_RESPONSE);

        networkFileDownloader.startDownloading(ANY_RAW_URL, UNKNOWN_FILE_SIZE, callback);

        verify(callback).onDownloadFinished();
    }

    @Test
    public void emitsError_whenRequestExecutionFails() throws IOException {
        given(httpClient.execute(requestCreator.createDownloadRequest(ANY_RAW_URL))).willThrow(IOException.class);

        networkFileDownloader.startDownloading(ANY_RAW_URL, UNKNOWN_FILE_SIZE, callback);

        verify(callback).onError();
    }

    @Test
    public void emitsDownloadFinished_whenRequestExecutionFails() throws IOException {
        given(httpClient.execute(requestCreator.createDownloadRequest(ANY_RAW_URL))).willThrow(IOException.class);

        networkFileDownloader.startDownloading(ANY_RAW_URL, UNKNOWN_FILE_SIZE, callback);

        verify(callback).onDownloadFinished();
    }

    @Ignore // How can we test the `canDownload` flag?
    @Test
    public void stopsEmittingBytes_whenStoppingDownload() throws IOException {
        networkFileDownloader.startDownloading(ANY_RAW_URL, UNKNOWN_FILE_SIZE, callback);

        networkFileDownloader.stopDownloading();

        verify(callback, never()).onBytesRead(new byte[10], 0);
    }
}
