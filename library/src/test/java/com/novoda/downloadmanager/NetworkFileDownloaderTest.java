package com.novoda.downloadmanager;

import com.novoda.notils.logger.simple.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static com.novoda.downloadmanager.InternalFileSizeFixtures.aFileSize;
import static com.novoda.downloadmanager.NetworkResponseFixtures.aNetworkResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class NetworkFileDownloaderTest {

    private static final byte[] BYTES_TO_RECEIVE = "s".getBytes();

    private static final String ANY_RAW_URL = "http://example.com";
    private static final FileSize KNOWN_FILE_SIZE = aFileSize().withAreBytesDownloadedKnown(true).build();
    private static final FileSize UNKNOWN_FILE_SIZE = aFileSize().withAreBytesDownloadedKnown(false).build();

    private final FileDownloader.Callback callback = mock(FileDownloader.Callback.class);
    private final HttpClient httpClient = mock(HttpClient.class);
    private final NetworkRequestCreator requestCreator = spy(new NetworkRequestCreator());

    private NetworkFileDownloader networkFileDownloader;

    @Before
    public void setUp() throws IOException {
        Log.setShowLogs(false);
        networkFileDownloader = new NetworkFileDownloader(httpClient, requestCreator);
        given(httpClient.execute(any(NetworkRequest.class))).willReturn(
                aNetworkResponse().withInputStream(new ByteArrayInputStream(BYTES_TO_RECEIVE)).build()
        );
    }

    @Test
    public void createsRequestWithDownloadedBytesHeader_whenKnown() {
        networkFileDownloader.startDownloading(ANY_RAW_URL, KNOWN_FILE_SIZE, callback);

        verify(requestCreator).createDownloadRequestWithDownloadedBytesHeader(ANY_RAW_URL, KNOWN_FILE_SIZE.currentSize(), KNOWN_FILE_SIZE.totalSize());
    }

    @Test
    public void createsDownloadRequest() {
        networkFileDownloader.startDownloading(ANY_RAW_URL, UNKNOWN_FILE_SIZE, callback);

        verify(requestCreator).createDownloadRequest(ANY_RAW_URL);
    }

    @Test
    public void emitsBytes_whenDownloading() {
        networkFileDownloader.startDownloading(ANY_RAW_URL, UNKNOWN_FILE_SIZE, callback);

        byte[] expectedBytes = new byte[4096];
        expectedBytes[0] = "s".getBytes()[0];

        verify(callback).onBytesRead(expectedBytes, 1);
    }

    @Test
    public void closesByteStream_whenDownloading() throws IOException {
        HttpClient.NetworkResponse networkResponse = spy(aNetworkResponse().build());
        given(httpClient.execute(any(NetworkRequest.class))).willReturn(networkResponse);

        networkFileDownloader.startDownloading(ANY_RAW_URL, UNKNOWN_FILE_SIZE, callback);

        verify(networkResponse).closeByteStream();
    }

    @Test
    public void emitsError_whenResponseCodeIsInvalid() throws IOException {
        given(httpClient.execute(any(NetworkRequest.class))).willReturn(aNetworkResponse().withCode(418).build());

        networkFileDownloader.startDownloading(ANY_RAW_URL, UNKNOWN_FILE_SIZE, callback);

        verify(callback).onError();
    }

    @Test
    public void emitsDownloadFinished_whenResponseCodeIsInvalid() throws IOException {
        given(httpClient.execute(any(NetworkRequest.class))).willReturn(aNetworkResponse().withCode(418).build());

        networkFileDownloader.startDownloading(ANY_RAW_URL, UNKNOWN_FILE_SIZE, callback);

        verify(callback).onDownloadFinished();
    }

    @Test
    public void emitsError_whenRequestExecutionFails() throws IOException {
        given(httpClient.execute(any(NetworkRequest.class))).willThrow(IOException.class);

        networkFileDownloader.startDownloading(ANY_RAW_URL, UNKNOWN_FILE_SIZE, callback);

        verify(callback).onError();
    }

    @Test
    public void emitsDownloadFinished_whenRequestExecutionFails() throws IOException {
        given(httpClient.execute(any(NetworkRequest.class))).willThrow(IOException.class);

        networkFileDownloader.startDownloading(ANY_RAW_URL, UNKNOWN_FILE_SIZE, callback);

        verify(callback).onDownloadFinished();
    }
}
