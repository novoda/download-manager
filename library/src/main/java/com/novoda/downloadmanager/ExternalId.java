package com.novoda.downloadmanager;

class ExternalId {

    public static final ExternalId NO_EXTERNAL_ID = new ExternalId("");

    private final String externalId;

    public ExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String asString() {
        return externalId;
    }
}
