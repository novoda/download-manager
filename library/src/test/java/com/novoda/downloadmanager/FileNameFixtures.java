package com.novoda.downloadmanager;

class FileNameFixtures {

    private String rawFileName = "rawFileName";

    static FileNameFixtures aFileName() {
        return new FileNameFixtures();
    }

    FileNameFixtures withRawFileName(String rawFileName) {
        this.rawFileName = rawFileName;
        return this;
    }

    FileName build() {
        return new FileName() {
            @Override
            public String name() {
                return rawFileName;
            }
        };
    }
}
