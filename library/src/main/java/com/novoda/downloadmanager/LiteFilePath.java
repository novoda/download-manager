package com.novoda.downloadmanager;

class LiteFilePath implements FilePath {

    private final String path;

    LiteFilePath(String path) {
        this.path = path;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public boolean isUnknown() {
        return path.equalsIgnoreCase(FilePathCreator.unknownFilePath().path());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LiteFilePath that = (LiteFilePath) o;

        return path != null ? path.equals(that.path) : that.path == null;
    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "LiteFilePath{"
                + "path='" + path + '\''
                + '}';
    }
}
