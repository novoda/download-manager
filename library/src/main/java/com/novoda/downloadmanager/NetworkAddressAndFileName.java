package com.novoda.downloadmanager;

class NetworkAddressAndFileName {
    private final String networkAddress;
    private final FileName fileName;

    NetworkAddressAndFileName(String networkAddress, FileName fileName) {
        this.networkAddress = networkAddress;
        this.fileName = fileName;
    }

    public String networkAddress() {
        return networkAddress;
    }

    public FileName fileName() {
        return fileName;
    }
}
