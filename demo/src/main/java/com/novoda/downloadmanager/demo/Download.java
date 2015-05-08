package com.novoda.downloadmanager.demo;

class Download {
    private final String title;
    private final String fileName;

    public Download(String title, String fileName) {
        this.title = title;
        this.fileName = fileName;
    }

    public String getTitle() {
        return title;
    }

    public String getFileName() {
        return fileName;
    }
}
