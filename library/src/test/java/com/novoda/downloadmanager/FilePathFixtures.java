package com.novoda.downloadmanager;

class FilePathFixtures {

    private String rawPath = "rawPath";
    private boolean isUnknown = false;

    static FilePathFixtures aFilePath() {
        return new FilePathFixtures();
    }

    FilePathFixtures withRawPath(String rawPath) {
        this.rawPath = rawPath;
        return this;
    }

    FilePathFixtures withUnknown(boolean unknown) {
        isUnknown = unknown;
        return this;
    }

    FilePath build() {
        return new FilePath() {
            @Override
            public String path() {
                return rawPath;
            }

            @Override
            public boolean isUnknown() {
                return isUnknown;
            }
        };
    }
}
