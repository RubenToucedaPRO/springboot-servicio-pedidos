package com.pedidos.domain.valueobjects;

import java.util.Objects;

import com.pedidos.domain.errors.InvalidQuantityException;

/**
 * Value object representing a quantity (integer > 0).
 */
public final class Quantity {
    private final int value;

    public Quantity(int value) {
        if (value <= 0)
            throw new InvalidQuantityException("Quantity must be > 0");
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public Quantity add(Quantity other) {
        Objects.requireNonNull(other);
        return new Quantity(this.value + other.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Quantity quantity = (Quantity) o;
        return value == quantity.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
