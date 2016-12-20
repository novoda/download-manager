package com.novoda.downloadmanager.domain;

public class ExternalId {

    public static final ExternalId NO_EXTERNAL_ID = new ExternalId("");

    private final String externalId;

    public ExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String asString() {
        return externalId;
    }
}
