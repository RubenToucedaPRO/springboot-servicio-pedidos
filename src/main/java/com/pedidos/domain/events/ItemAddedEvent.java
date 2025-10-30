package com.pedidos.domain.events;

import java.time.Instant;

import com.pedidos.domain.valueobjects.Money;
import com.pedidos.domain.valueobjects.OrderId;
import com.pedidos.domain.valueobjects.ProductId;
import com.pedidos.domain.valueobjects.Quantity;

public final class ItemAddedEvent {
    private final OrderId orderId;
    private final ProductId productId;
    private final Quantity quantity;
    private final Money unitPrice;
    private final Instant occurredAt;

    public ItemAddedEvent(OrderId orderId, ProductId productId, Quantity quantity, Money unitPrice,
            Instant occurredAt) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.occurredAt = occurredAt;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public ProductId getProductId() {
        return productId;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
