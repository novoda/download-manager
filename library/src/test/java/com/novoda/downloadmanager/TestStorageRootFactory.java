package com.novoda.downloadmanager;

public final class TestStorageRootFactory {

    private TestStorageRootFactory() {
        // Uses static factory methods.
    }

    public static StorageRoot create() {
        return new TestStorageRoot();
    }

    static class TestStorageRoot implements StorageRoot {

        private final String path = "root";

        @Override
        public String path() {
            return path;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            TestStorageRoot that = (TestStorageRoot) o;

            return path != null ? path.equals(that.path) : that.path == null;
        }

        @Override
        public int hashCode() {
            return path != null ? path.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "TestStorageRoot{"
                    + "path='" + path + '\''
                    + '}';
        }
    }
}
