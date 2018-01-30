package com.novoda.downloadmanager;

final class Optional<T> {

    @SuppressWarnings("unchecked")  // Type erasure has us covered here, we don't care
    private static final Optional ABSENT = new Optional(null);

    private final T data;

    @SuppressWarnings("unchecked")  // Type erasure has us covered here, we don't care
    static <T> Optional<T> absent() {
        return ABSENT;
    }

    static <T> Optional<T> fromNullable(T data) {
        if (data == null) {
            return absent();
        }
        return new Optional<>(data);
    }

    static <T> Optional<T> of(T data) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null. Use Optional.fromNullable(maybeNullData).");
        }
        return new Optional<>(data);
    }

    private Optional(T data) {
        this.data = data;
    }

    boolean isPresent() {
        return data != null;
    }

    boolean isAbsent() {
        return !isPresent();
    }

    T get() {
        if (!isPresent()) {
            throw new IllegalStateException("You must check if data is present before using get()");
        }
        return data;
    }

    T or(T elseCase) {
        return isPresent() ? get() : elseCase;
    }

    Optional<T> or(Optional<T> elseCase) {
        return isPresent() ? this : elseCase;
    }

    Optional<T> or(Func0<Optional<T>> elseFunc) {
        return isPresent() ? this : elseFunc.call();
    }

    interface Func0<V> {

        V call();
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
        return String.format("Optional<%s>", isAbsent() ? "Absent" : data.toString());
    }
}
