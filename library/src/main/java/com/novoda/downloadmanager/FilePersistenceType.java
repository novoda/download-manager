package com.novoda.downloadmanager;

import java.security.InvalidParameterException;

public enum FilePersistenceType {
    INTERNAL("internal"),
    EXTERNAL("external"),
    CUSTOM("custom");

    private final String rawValue;

    FilePersistenceType(String rawValue) {
        this.rawValue = rawValue;
    }

    static FilePersistenceType from(String rawValue) {
        for (FilePersistenceType filePersistenceType : values()) {
            if (filePersistenceType.rawValue.equals(rawValue)) {
                return filePersistenceType;
            }
        }

        throw new InvalidParameterException("Type " + rawValue + " is not supported");
    }

    String toRawValue() {
        return rawValue;
    }
}
