package com.novoda.downloadmanager;

class NetworkAddressAndFilePath {

    private final String networkAddress;
    private final FilePath filePath;

    NetworkAddressAndFilePath(String networkAddress, FilePath filePath) {
        this.networkAddress = networkAddress;
        this.filePath = filePath;
    }

    String networkAddress() {
        return networkAddress;
    }

    FilePath filePath() {
        return filePath;
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
        return filePath != null ? filePath.equals(that.filePath) : that.filePath == null;
    }

    @Override
    public int hashCode() {
        int result = networkAddress != null ? networkAddress.hashCode() : 0;
        result = 31 * result + (filePath != null ? filePath.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "NetworkAddressAndFilePath{"
                + "networkAddress='" + networkAddress + '\''
                + ", filePath=" + filePath
                + '}';
    }
}
