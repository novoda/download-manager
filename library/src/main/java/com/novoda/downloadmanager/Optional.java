package com.novoda.downloadmanager;

public final class Optional<T> {

    @SuppressWarnings("unchecked")  // Type erasure has us covered here, we don't care
    private static final Optional ABSENT = new Optional(null);

    private final T data;

    @SuppressWarnings("unchecked")  // Type erasure has us covered here, we don't care
    public static <T> Optional<T> absent() {
        return ABSENT;
    }

    public static <T> Optional<T> of(T data) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null. Use Optional.fromNullable(maybeNullData).");
        }
        return new Optional<>(data);
    }

    private Optional(T data) {
        this.data = data;
    }

    public boolean isPresent() {
        return data != null;
    }

    public T get() {
        if (!isPresent()) {
            throw new IllegalStateException("You must check if data is present before using get()");
        }
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Optional<?> optional = (Optional<?>) o;

        return data != null ? data.equals(optional.data) : optional.data == null;
    }

    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Optional{" +
                "data=" + data +
                '}';
    }
}
