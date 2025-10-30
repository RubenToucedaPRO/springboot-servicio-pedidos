package com.pedidos.shared.result;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Simple Result type for use cases: either Ok<T,E> or Fail<T,E>.
 * Implemented as a sealed interface with record variants (Java 17).
 */
public sealed interface Result<T, E> permits Result.Ok, Result.Fail {

    boolean isOk();

    boolean isFail();

    T getValue();

    E getError();

    static <T, E> Result<T, E> ok(T value) {
        return new Ok<>(value);
    }

    static <T, E> Result<T, E> fail(E error) {
        return new Fail<>(error);
    }

    default <U> Result<U, E> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (isOk()) {
            return ok(mapper.apply(getValue()));
        }
        @SuppressWarnings("unchecked")
        Result<U, E> self = (Result<U, E>) this;
        return self;
    }

    default <U> Result<U, E> flatMap(Function<? super T, ? extends Result<U, E>> mapper) {
        Objects.requireNonNull(mapper);
        if (isOk()) {
            return Objects.requireNonNull(mapper.apply(getValue()));
        }
        @SuppressWarnings("unchecked")
        Result<U, E> self = (Result<U, E>) this;
        return self;
    }

    default Result<T, E> onFailure(Supplier<? extends E> supplier) {
        if (isFail()) {
            supplier.get(); // allow side-effects; value ignored
        }
        return this;
    }

    record Ok<T, E>(T value) implements Result<T, E> {
        @Override
        public boolean isOk() {
            return true;
        }

        @Override
        public boolean isFail() {
            return false;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public E getError() {
            throw new IllegalStateException("No error present in Ok");
        }
    }

    record Fail<T, E>(E error) implements Result<T, E> {
        @Override
        public boolean isOk() {
            return false;
        }

        @Override
        public boolean isFail() {
            return true;
        }

        @Override
        public T getValue() {
            throw new IllegalStateException("No value present in Fail");
        }

        @Override
        public E getError() {
            return error;
        }
    }
}
