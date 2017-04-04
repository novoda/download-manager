package com.novoda.downloadmanager;

interface ClientCheckResult {

    ClientCheckResult ALLOWED = new ClientCheckResult() {
        @Override
        public boolean isAllowed() {
            return true;
        }

        @Override
        public String reason() {
            throw new IllegalAccessError("Not implemented for allowed downloads");
        }

        @Override
        public DownloadId id() {
            throw new IllegalAccessError("Not implemented for allowed downloads");
        }
    };

    boolean isAllowed();

    String reason();

    DownloadId id();


}
