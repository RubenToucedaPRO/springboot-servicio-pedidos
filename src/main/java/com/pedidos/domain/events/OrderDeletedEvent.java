package com.pedidos.domain.events;

import java.time.Instant;

import com.pedidos.domain.valueobjects.OrderId;

/**
 * Domain event emitted when an order is deleted.
 */
public final class OrderDeletedEvent {
    private final OrderId orderId;
    private final Instant deletedAt;
    private final String reason; // optional reason for deletion

    public OrderDeletedEvent(OrderId orderId, Instant deletedAt, String reason) {
        this.orderId = orderId;
        this.deletedAt = deletedAt;
        this.reason = reason;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public String getReason() {
        return reason;
    }
}
