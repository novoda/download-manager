package com.novoda.downloadmanager;

class NetworkAddressAndFilePath {

    private final String networkAddress;
    private final String relativePathToStoreDownload;

    NetworkAddressAndFilePath(String networkAddress, String relativePathToStoreDownload) {
        this.networkAddress = networkAddress;
        this.relativePathToStoreDownload = relativePathToStoreDownload;
    }

    String networkAddress() {
        return networkAddress;
    }

    String relativePathToStoreDownload() {
        return relativePathToStoreDownload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NetworkAddressAndFilePath that = (NetworkAddressAndFilePath) o;

        if (networkAddress != null ? !networkAddress.equals(that.networkAddress) : that.networkAddress != null) {
            return false;
        }
        return relativePathToStoreDownload != null
                ? relativePathToStoreDownload.equals(that.relativePathToStoreDownload) : that.relativePathToStoreDownload == null;
    }

    @Override
    public int hashCode() {
        int result = networkAddress != null ? networkAddress.hashCode() : 0;
        result = 31 * result + (relativePathToStoreDownload != null ? relativePathToStoreDownload.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "NetworkAddressAndFilePath{"
                + "networkAddress='" + networkAddress + '\''
                + ", relativePathToStoreDownload='" + relativePathToStoreDownload + '\''
                + '}';
    }
}
