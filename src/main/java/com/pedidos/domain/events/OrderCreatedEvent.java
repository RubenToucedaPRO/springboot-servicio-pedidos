package com.pedidos.domain.events;

import java.time.Instant;

import com.pedidos.domain.valueobjects.OrderId;

public final class OrderCreatedEvent {
    private final OrderId orderId;
    private final Instant createdAt;

    public OrderCreatedEvent(OrderId orderId, Instant createdAt) {
        this.orderId = orderId;
        this.createdAt = createdAt;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
