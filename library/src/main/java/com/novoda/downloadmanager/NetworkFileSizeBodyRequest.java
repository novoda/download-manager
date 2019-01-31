package com.novoda.downloadmanager;

import java.io.IOException;

class NetworkFileSizeBodyRequest implements FileSizeRequester {

    private final HttpClient httpClient;
    private final NetworkRequestCreator requestCreator;

    NetworkFileSizeBodyRequest(HttpClient httpClient, NetworkRequestCreator requestCreator) {
        this.httpClient = httpClient;
        this.requestCreator = requestCreator;
    }

    @Override
    public void requestFileSize(String url, Callback callback) {
        NetworkRequest fileSizeRequest = requestCreator.createFileSizeBodyRequest(url);
        NetworkResponse response = null;
        try {
            response = httpClient.execute(fileSizeRequest);
            processResponse(callback, response, url);
        } catch (IOException e) {
            callback.onError(e.getMessage());
        } finally {
            if (response != null) {
                try {
                    response.closeByteStream();
                } catch (IOException e) {
                    callback.onError(e.getMessage());
                }
            }
        }
    }

    private void processResponse(Callback callback, NetworkResponse response, String url) {
        if (response.isSuccessful()) {
            long rawFileSize = response.bodyContentLength();
            callback.onFileSizeReceived(FileSizeCreator.createFromTotalSize(rawFileSize));
        } else {
            Logger.e("Network response code is not ok, responseCode: " + response.code());
            String networkErrorMessage = String.format(
                    "File Size Body Request: %s with response code: %s failed.",
                    url,
                    response.code()
            );
            callback.onError(networkErrorMessage);
        }
    }

    @Override
    public FileSize requestFileSize(String url) {
        return null;
    }
}
