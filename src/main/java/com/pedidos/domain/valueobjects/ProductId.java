package com.pedidos.domain.valueobjects;

/**
 * Simple value object for product identity.
 */
public final class ProductId {
    private final String id;

    public ProductId(String id) {
        if (id == null)
            throw new com.pedidos.domain.errors.InvalidProductIdException("product id must not be null");
        String trimmed = id.trim();
        if (trimmed.isEmpty())
            throw new com.pedidos.domain.errors.InvalidProductIdException("product id must not be empty");
        this.id = trimmed;
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
