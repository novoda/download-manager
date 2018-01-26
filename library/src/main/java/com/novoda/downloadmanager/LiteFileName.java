package com.novoda.downloadmanager;

final class LiteFileName implements FileName {

    private final String name;

    static LiteFileName from(Batch batch, String fileUrl) {
        String name = batch + fileUrl + System.nanoTime();
        return new LiteFileName(String.valueOf(name.hashCode()));
    }

    static LiteFileName from(String name) {
        return new LiteFileName(name);
    }

    private LiteFileName(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LiteFileName that = (LiteFileName) o;

        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "LiteFileName{"
                + "name='" + name + '\''
                + '}';
    }
}
