package com.novoda.downloadmanager.demo;

import com.novoda.downloadmanager.HttpClient;
import com.novoda.downloadmanager.NetworkRequest;

import java.io.IOException;

public class CustomHttpClient implements HttpClient {

    @Override
    public NetworkResponse execute(NetworkRequest networkRequest) throws IOException {
        return null;
    }
}
