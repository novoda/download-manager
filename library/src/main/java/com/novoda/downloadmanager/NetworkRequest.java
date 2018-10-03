package com.novoda.downloadmanager;

import java.util.Map;

/**
 * Represents the minimum set of information that will be passed to
 * a {@link HttpClient} to perform a request.
 */
public interface NetworkRequest {

    /**
     * @return the headers associated with a request.
     */
    Map<String, String> headers();

    /**
     * @return the address for the asset to download.
     */
    String url();

    /**
     * @return the method used when requesting a resource.
     */
    Method method();

    enum Method {
        GET("get"),
        HEAD("head");

        private final String rawMethod;

        Method(String rawMethod) {
            this.rawMethod = rawMethod;
        }

        public String rawMethod() {
            return rawMethod;
        }
    }
}
