package com.novoda.downloadmanager;

import java.io.IOException;

/**
 * Used to customize the http client that will be used to request file sizes
 * and download assets. Clients of this library can create their own
 * implementation and pass it to {@link DownloadManagerBuilder#withCustomHttpClient(HttpClient)}.
 */
public interface HttpClient {

    /**
     * Executes a given {@link NetworkRequest} and passes back a {@link NetworkResponse}.
     *
     * @param networkRequest to perform.
     * @return the {@link NetworkResponse} of the executed {@link NetworkRequest}.
     * @throws IOException when processing a response stream.
     */
    NetworkResponse execute(NetworkRequest networkRequest) throws IOException;

}
