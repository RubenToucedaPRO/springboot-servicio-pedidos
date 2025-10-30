package com.pedidos.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object wrapping a UUID for orders.
 */
public final class OrderId {
    private final UUID id;

    public OrderId(UUID id) {
        this.id = Objects.requireNonNull(id, "order id must not be null");
    }

    public static OrderId newId() {
        return new OrderId(UUID.randomUUID());
    }

    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OrderId orderId = (OrderId) o;
        return id.equals(orderId.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
