package com.pedidos.domain.valueobjects;

import java.util.Objects;

/**
 * Simple value object for product identity.
 */
public final class ProductId {
    private final String id;

    public ProductId(String id) {
        this.id = Objects.requireNonNull(id, "product id must not be null").trim();
        if (this.id.isEmpty())
            throw new IllegalArgumentException("product id must not be empty");
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ProductId productId = (ProductId) o;
        return id.equals(productId.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
