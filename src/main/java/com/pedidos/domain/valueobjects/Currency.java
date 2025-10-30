package com.pedidos.domain.valueobjects;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Value object representing a currency code. Currently only supports EUR and
 * USD.
 * You can extend supported currencies by adding codes to SUPPORTED.
 */
public final class Currency {
    private static final Set<String> SUPPORTED;

    static {
        Set<String> s = new HashSet<>();
        s.add("EUR");
        s.add("USD");
        SUPPORTED = Collections.unmodifiableSet(s);
    }

    private final String code;

    private Currency(String code) {
        this.code = code;
    }

    public static Currency of(String code) {
        Objects.requireNonNull(code, "currency code must not be null");
        String normalized = code.trim().toUpperCase();
        if (!SUPPORTED.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported currency: " + normalized);
        }
        return new Currency(normalized);
    }

    public static Currency EUR() {
        return new Currency("EUR");
    }

    public static Currency USD() {
        return new Currency("USD");
    }

    public String getCode() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Currency currency = (Currency) o;
        return code.equals(currency.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return code;
    }
}
