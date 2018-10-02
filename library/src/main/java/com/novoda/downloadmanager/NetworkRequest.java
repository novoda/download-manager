package com.novoda.downloadmanager;

import java.util.Map;

public interface NetworkRequest {

    Map<String, String> headers();

    String url();

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
