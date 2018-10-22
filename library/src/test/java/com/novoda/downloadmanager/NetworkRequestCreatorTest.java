package com.novoda.downloadmanager;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.novoda.downloadmanager.NetworkRequestFixtures.aNetworkRequest;

public class NetworkRequestCreatorTest {

    private final NetworkRequestCreator networkRequestCreator = new NetworkRequestCreator();

    @Test
    public void createsFileSizeHeadRequest() {
        NetworkRequest networkRequest = networkRequestCreator.createFileSizeHeadRequest("http://www.google.com");

        NetworkRequest expectedNetworkRequest = aNetworkRequest()
                .withHeader("Accept-Encoding", "identity")
                .withUrl("http://www.google.com")
                .withMethod(NetworkRequest.Method.HEAD)
                .build();

        assertThatNetworkRequestsAreEqual(networkRequest, expectedNetworkRequest);
    }

    @Test
    public void createsFileSizeBodyRequest() {
        NetworkRequest networkRequest = networkRequestCreator.createFileSizeBodyRequest("http://www.google.com");

        NetworkRequest expectedNetworkRequest = aNetworkRequest()
                .withHeader("Accept-Encoding", "identity")
                .withUrl("http://www.google.com")
                .withMethod(NetworkRequest.Method.GET)
                .build();

        assertThatNetworkRequestsAreEqual(networkRequest, expectedNetworkRequest);
    }

    @Test
    public void givenSimpleRequest_whenCreatingRequest_thenReturnsExpectedResumeRequest() {
        NetworkRequest networkRequest = networkRequestCreator.createDownloadRequest("http://www.google.com");

        NetworkRequest expectedNetworkRequest = aNetworkRequest()
                .withUrl("http://www.google.com")
                .withMethod(NetworkRequest.Method.GET)
                .build();

        assertThatNetworkRequestsAreEqual(networkRequest, expectedNetworkRequest);
    }

    @Test
    public void givenPartiallyDownloadedFile_whenCreatingResumeRequest_thenReturnsExpectedResumeRequest() {
        NetworkRequest networkRequest = networkRequestCreator.createDownloadRequestWithDownloadedBytesHeader(
                "http://www.google.com",
                100,
                500);

        NetworkRequest expectedNetworkRequest = aNetworkRequest()
                .withHeader("Range", "bytes=100-499")
                .withUrl("http://www.google.com")
                .withMethod(NetworkRequest.Method.GET)
                .build();

        assertThatNetworkRequestsAreEqual(networkRequest, expectedNetworkRequest);
    }

    private void assertThatNetworkRequestsAreEqual(NetworkRequest networkRequest, NetworkRequest expectedNetworkRequest) {
        assertThat(networkRequest.headers()).isEqualTo(expectedNetworkRequest.headers());
        assertThat(networkRequest.method()).isEqualTo(expectedNetworkRequest.method());
        assertThat(networkRequest.url()).isEqualTo(expectedNetworkRequest.url());
    }
}
