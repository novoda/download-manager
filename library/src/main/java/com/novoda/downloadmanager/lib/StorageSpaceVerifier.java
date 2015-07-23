package com.novoda.downloadmanager.lib;

class StorageSpaceVerifier implements SpaceVerifier {

    private final StorageManager storageManager;
    private final int destination;
    private final String fileName;

    public StorageSpaceVerifier(StorageManager storageManager, int destination, String fileName) {
        this.storageManager = storageManager;
        this.destination = destination;
        this.fileName = fileName;
    }

    @Override
    public void verifySpacePreemptively(int count) throws StopRequestException {
        storageManager.verifySpaceBeforeWritingToFile(destination, fileName, count);
    }

    @Override
    public void verifySpace(int count) throws StopRequestException {
        storageManager.verifySpace(destination, fileName, count);
    }
}
