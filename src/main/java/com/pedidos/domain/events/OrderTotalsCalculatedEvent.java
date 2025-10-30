package com.pedidos.domain.events;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import com.pedidos.domain.valueobjects.Currency;
import com.pedidos.domain.valueobjects.Money;
import com.pedidos.domain.valueobjects.OrderId;

public final class OrderTotalsCalculatedEvent {
    private final OrderId orderId;
    private final Map<Currency, Money> totalsByCurrency;
    private final Instant occurredAt;

    public OrderTotalsCalculatedEvent(OrderId orderId, Map<Currency, Money> totalsByCurrency, Instant occurredAt) {
        this.orderId = orderId;
        this.totalsByCurrency = Collections.unmodifiableMap(totalsByCurrency);
        this.occurredAt = occurredAt;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public Map<Currency, Money> getTotalsByCurrency() {
        return totalsByCurrency;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
