package com.pedidos.domain.entities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.pedidos.domain.events.ItemAddedEvent;
import com.pedidos.domain.events.ItemRemovedEvent;
import com.pedidos.domain.events.OrderCreatedEvent;
import com.pedidos.domain.events.OrderTotalsCalculatedEvent;
import com.pedidos.domain.valueobjects.Money;
import com.pedidos.domain.valueobjects.OrderId;
import com.pedidos.domain.valueobjects.OrderItem;
import com.pedidos.domain.valueobjects.ProductId;

/**
 * Aggregate root for Order. No frameworks used — plain Java entity with domain
 * events list.
 */
public class Order {
    private final OrderId id;
    private final Map<ProductId, OrderItem> items = new LinkedHashMap<>();
    private final List<Object> domainEvents = new ArrayList<>();

    private Order(OrderId id) {
        this.id = Objects.requireNonNull(id, "order id must not be null");
        domainEvents.add(new OrderCreatedEvent(id, Instant.now()));
    }

    public static Order create(OrderId id) {
        return new Order(id);
    }

    public OrderId getId() {
        return id;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(items.values()));
    }

    public List<Object> pullDomainEvents() {
        List<Object> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

    public void addItem(OrderItem item) {
        Objects.requireNonNull(item);
        ProductId pid = item.getProductId();
        if (items.containsKey(pid)) {
            OrderItem existing = items.get(pid);
            OrderItem merged = existing.increaseQuantity(item.getQuantity());
            items.put(pid, merged);
        } else {
            items.put(pid, item);
        }
        domainEvents.add(new ItemAddedEvent(id, pid, item.getQuantity(), item.getUnitPrice(), Instant.now()));
    }

    public void removeItem(ProductId productId) {
        Objects.requireNonNull(productId);
        if (items.remove(productId) != null) {
            domainEvents.add(new ItemRemovedEvent(id, productId, Instant.now()));
        }
    }

    /**
     * Calcula los totales agrupados por moneda.
     */
    public Map<com.pedidos.domain.valueobjects.Currency, Money> totalsByCurrency() {
        Map<com.pedidos.domain.valueobjects.Currency, Money> totals = new LinkedHashMap<>();
        for (OrderItem item : items.values()) {
            Money lineTotal = item.total();
            com.pedidos.domain.valueobjects.Currency cur = lineTotal.getCurrency();
            totals.merge(cur, lineTotal, (a, b) -> a.add(b));
        }
        domainEvents.add(new OrderTotalsCalculatedEvent(id, totals, Instant.now()));
        return Collections.unmodifiableMap(totals);
    }

    public Money totalForCurrency(com.pedidos.domain.valueobjects.Currency currency) {
        Objects.requireNonNull(currency);
        Map<com.pedidos.domain.valueobjects.Currency, Money> totals = totalsByCurrency();
        return totals.getOrDefault(currency, Money.zero(currency));
    }
}
