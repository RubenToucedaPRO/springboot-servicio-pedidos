package com.pedidos.domain.events;

import java.time.Instant;

import com.pedidos.domain.valueobjects.OrderId;
import com.pedidos.domain.valueobjects.ProductId;

public final class ItemRemovedEvent {
    private final OrderId orderId;
    private final ProductId productId;
    private final Instant occurredAt;

    public ItemRemovedEvent(OrderId orderId, ProductId productId, Instant occurredAt) {
        this.orderId = orderId;
        this.productId = productId;
        this.occurredAt = occurredAt;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public ProductId getProductId() {
        return productId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
