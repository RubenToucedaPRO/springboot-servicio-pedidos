package com.pedidos.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.pedidos.domain.events.ItemAddedEvent;
import com.pedidos.domain.events.ItemRemovedEvent;
import com.pedidos.domain.events.OrderCreatedEvent;
import com.pedidos.domain.events.OrderTotalsCalculatedEvent;
import com.pedidos.domain.valueobjects.Currency;
import com.pedidos.domain.valueobjects.Money;
import com.pedidos.domain.valueobjects.OrderId;
import com.pedidos.domain.valueobjects.OrderItem;
import com.pedidos.domain.valueobjects.ProductId;
import com.pedidos.domain.valueobjects.Quantity;

class OrderTest {

    @Test
    void createProducesCreatedEvent() {
        OrderId id = OrderId.newId();
        Order order = Order.create(id);
        List<Object> events = order.pullDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof OrderCreatedEvent);
    }

    @Test
    void addItemAndTotalsAndRemove() {
        Order order = Order.create(OrderId.newId());
        ProductId pid = new ProductId("pA");
        OrderItem item = new OrderItem(pid, new Quantity(2), new Money(BigDecimal.valueOf(3), Currency.EUR()));

        order.addItem(item);
        // after add, items should contain one entry
        assertEquals(1, order.getItems().size());

        // totals
        Map<Currency, Money> totals = order.totalsByCurrency();
        assertEquals(1, totals.size());
        Money total = totals.get(Currency.EUR());
        assertNotNull(total);
        assertEquals(new BigDecimal("6.00"), total.getAmount());

        // events: ItemAddedEvent created when adding, and OrderTotalsCalculatedEvent
        // when calculating totals
        List<Object> events = order.pullDomainEvents();
        // after pullDomainEvents previous events (OrderCreated + ItemAdded +
        // OrderTotalsCalculated) might be present
        assertTrue(events.stream().anyMatch(e -> e instanceof ItemAddedEvent));
        assertTrue(events.stream().anyMatch(e -> e instanceof OrderTotalsCalculatedEvent));

        // add same product again -> merge quantities
        OrderItem another = new OrderItem(pid, new Quantity(3), new Money(BigDecimal.valueOf(3), Currency.EUR()));
        order.addItem(another);
        assertEquals(1, order.getItems().size());
        assertEquals(5, order.getItems().get(0).getQuantity().getValue());

        // remove item
        order.removeItem(pid);
        assertEquals(0, order.getItems().size());
        List<Object> afterRemoveEvents = order.pullDomainEvents();
        assertTrue(afterRemoveEvents.stream().anyMatch(e -> e instanceof ItemRemovedEvent));
    }
}
