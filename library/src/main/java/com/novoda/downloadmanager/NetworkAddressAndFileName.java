package com.novoda.downloadmanager;

class NetworkAddressAndFileName {

    private final String networkAddress;
    private final FileName fileName;

    NetworkAddressAndFileName(String networkAddress, FileName fileName) {
        this.networkAddress = networkAddress;
        this.fileName = fileName;
    }

    String networkAddress() {
        return networkAddress;
    }

    FileName fileName() {
        return fileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NetworkAddressAndFileName that = (NetworkAddressAndFileName) o;

        if (networkAddress != null ? !networkAddress.equals(that.networkAddress) : that.networkAddress != null) {
            return false;
        }
        return fileName != null ? fileName.equals(that.fileName) : that.fileName == null;
    }

    @Override
    public int hashCode() {
        int result = networkAddress != null ? networkAddress.hashCode() : 0;
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "NetworkAddressAndFileName{"
                + "networkAddress='" + networkAddress + '\''
                + ", fileName=" + fileName
                + '}';
    }
}
