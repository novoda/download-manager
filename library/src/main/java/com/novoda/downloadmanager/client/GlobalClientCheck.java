package com.novoda.downloadmanager.client;

import java.io.Serializable;

public interface GlobalClientCheck extends Serializable {

    GlobalClientCheck IGNORED = new GlobalClientCheck() {
        @Override
        public ClientCheckResult onGlobalCheck() {
            return ClientCheckResult.ALLOWED;
        }
    };

    ClientCheckResult onGlobalCheck();

}
