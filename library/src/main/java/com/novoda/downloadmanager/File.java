package com.novoda.downloadmanager;

public class File {

    private final String networkAddress;
    private final Optional<FileName> fileName;
    private final Optional<String> relativePath;

    File(String networkAddress, Optional<FileName> fileName, Optional<String> relativePath) {
        this.networkAddress = networkAddress;
        this.fileName = fileName;
        this.relativePath = relativePath;
    }

    static File.Builder newBuilder(String networkAddress) {
        return new Builder(networkAddress);
    }

    public String networkAddress() {
        return networkAddress;
    }

    public Optional<FileName> fileName() {
        return fileName;
    }

    public Optional<String> relativePath() {
        return relativePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        File file = (File) o;

        if (networkAddress != null ? !networkAddress.equals(file.networkAddress) : file.networkAddress != null) {
            return false;
        }
        if (fileName != null ? !fileName.equals(file.fileName) : file.fileName != null) {
            return false;
        }
        return relativePath != null ? relativePath.equals(file.relativePath) : file.relativePath == null;
    }

    @Override
    public int hashCode() {
        int result = networkAddress != null ? networkAddress.hashCode() : 0;
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + (relativePath != null ? relativePath.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "File{"
                + "networkAddress='" + networkAddress + '\''
                + ", fileName=" + fileName
                + ", relativePath=" + relativePath
                + '}';
    }

    public static class Builder {

        private final String networkAddress;
        private Optional<FileName> fileName = Optional.absent();
        private Optional<String> relativePath = Optional.absent();

        private BatchTemp.Builder parentBuilder;

        public Builder(String networkAddress) {
            this.networkAddress = networkAddress;
        }

        Builder withParentBuilder(BatchTemp.Builder parentBuilder) {
            this.parentBuilder = parentBuilder;
            return this;
        }

        public Builder withFileName(FileName fileName) {
            this.fileName = Optional.of(fileName);
            return this;
        }

        public Builder withRelativePath(String relativePath) {
            this.relativePath = Optional.of(relativePath);
            return this;
        }

        public BatchTemp.Builder apply() {
            parentBuilder.withFile(new File(networkAddress, fileName, relativePath));
            return parentBuilder;
        }

    }
}
