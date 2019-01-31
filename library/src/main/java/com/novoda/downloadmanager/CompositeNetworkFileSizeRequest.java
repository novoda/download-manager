package com.novoda.downloadmanager;

class CompositeNetworkFileSizeRequest implements FileSizeRequester {

    private static final int UNKNOWN_CONTENT_LENGTH = -1;
    private static final int ZERO_FILE_SIZE = 0;

    private final NetworkFileSizeHeaderRequest headerRequest;
    private final NetworkFileSizeBodyRequest bodyRequest;

    CompositeNetworkFileSizeRequest(NetworkFileSizeHeaderRequest headerRequest, NetworkFileSizeBodyRequest bodyRequest) {
        this.headerRequest = headerRequest;
        this.bodyRequest = bodyRequest;
    }

    @Override
    public FileSize requestFileSize(String url) {
        return null;
    }

    @Override
    public void requestFileSize(String url, Callback callback) {
        headerRequest.requestFileSize(url, new HeaderRequestCallback(url, bodyRequest, callback));
    }

    private class HeaderRequestCallback implements Callback {

        private final String url;
        private final Callback callback;
        private final NetworkFileSizeBodyRequest bodyRequest;

        HeaderRequestCallback(String url, NetworkFileSizeBodyRequest bodyRequest, Callback callback) {
            this.url = url;
            this.bodyRequest = bodyRequest;
            this.callback = callback;
        }

        @Override
        public void onFileSizeReceived(FileSize fileSize) {
            if (fileSize.totalSize() == UNKNOWN_CONTENT_LENGTH || fileSize.totalSize() == ZERO_FILE_SIZE) {
                bodyRequest.requestFileSize(url, new BodyRequestCallback(url, callback));
            }
        }

        @Override
        public void onError(String message) {
            bodyRequest.requestFileSize(url, new BodyRequestCallback(url, callback));
        }
    }

    private class BodyRequestCallback implements Callback {

        private final String url;
        private final Callback callback;

        BodyRequestCallback(String url, Callback callback) {
            this.url = url;
            this.callback = callback;
        }

        @Override
        public void onFileSizeReceived(FileSize fileSize) {
            if (fileSize.totalSize() == UNKNOWN_CONTENT_LENGTH || fileSize.totalSize() == ZERO_FILE_SIZE) {
                callback.onError("File size unknown for request: " + url);
            }
        }

        @Override
        public void onError(String message) {
            callback.onError(message);
        }
    }
}
