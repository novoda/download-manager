package com.novoda.downloadmanager;

import java.security.InvalidParameterException;

enum FileDownloaderType {
    NETWORK("network"),
    CUSTOM("custom");

    private final String rawValue;

    FileDownloaderType(String rawValue) {
        this.rawValue = rawValue;
    }

    static FileDownloaderType from(String rawValue) {
        for (FileDownloaderType fileDownloaderType : values()) {
            if (fileDownloaderType.rawValue.equals(rawValue)) {
                return fileDownloaderType;
            }
        }

        throw new InvalidParameterException("Type " + rawValue + " is not supported");
    }
}
