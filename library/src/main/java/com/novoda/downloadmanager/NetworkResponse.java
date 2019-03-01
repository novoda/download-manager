package com.novoda.downloadmanager;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents the minimum set of information that is required in order
 * to process a network response for a download.
 */
public interface NetworkResponse {

    /**
     * @return the HTTP status code.
     */
    int code();

    /**
     * @return a status message corresponding to {@link NetworkResponse#code()}.
     */
    String statusMessage();

    /**
     * @return whether the request resulted in a successful response.
     * Ordinarily this will be tied to the {@link NetworkResponse#code()}.
     */
    boolean isSuccessful();

    /**
     * Retrieve a header value if present, otherwise retrieve a default value.
     *
     * @param name         of the header.
     * @param defaultValue to use if the header is not present.
     * @return the value of the given header.
     */
    String header(String name, String defaultValue);

    /**
     * @return an {@link InputStream} representing the body of the response.
     * @throws IOException when processing an {@link InputStream}.
     */
    InputStream openByteStream() throws IOException;

    /**
     * Closes the {@link InputStream} from {@link NetworkResponse#openByteStream()}.
     *
     * @throws IOException when failing to close an {@link InputStream}.
     */
    void closeByteStream() throws IOException;

    /**
     * @return the body content length in bytes for a response.
     */
    long bodyContentLength();
}
