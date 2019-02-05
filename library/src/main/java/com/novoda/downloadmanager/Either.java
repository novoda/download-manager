package com.novoda.downloadmanager;

public final class Either<T, V> {

    private final T left;
    private final V right;

    public static <T, V> Either<T, V> asLeft(T value) {
        return new Either<>(value, null);
    }

    public static <T, V> Either<T, V> asRight(V value) {
        return new Either<>(null, value);
    }

    private Either(T left, V right) {
        this.left = left;
        this.right = right;
    }

    public T left() {
        if (left == null) {
            throw new IllegalArgumentException("You must check if it's a left or right before querying");
        }
        return left;
    }

    public V right() {
        if (right == null) {
            throw new IllegalArgumentException("You must check if it's a left or right before querying");
        }
        return right;
    }

    public boolean isLeft() {
        return left != null;
    }

    public boolean isRight() {
        return right != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Either<?, ?> either = (Either<?, ?>) o;

        if (left != null ? !left.equals(either.left) : either.left != null) {
            return false;
        }
        return right != null ? right.equals(either.right) : either.right == null;
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }
}
