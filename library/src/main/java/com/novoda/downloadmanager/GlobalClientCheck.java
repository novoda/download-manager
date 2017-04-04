package com.novoda.downloadmanager;

import java.io.Serializable;

interface GlobalClientCheck extends Serializable {

    GlobalClientCheck IGNORED = new GlobalClientCheck() {
        @Override
        public ClientCheckResult onGlobalCheck() {
            return ClientCheckResult.ALLOWED;
        }
    };

    ClientCheckResult onGlobalCheck();

}
