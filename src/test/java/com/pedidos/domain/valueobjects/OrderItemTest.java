package com.pedidos.domain.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class OrderItemTest {

    @Test
    void nullArgumentsThrow() {
        ProductId pid = new ProductId("p1");
        Quantity q = new Quantity(1);
        Money price = new Money(BigDecimal.valueOf(10), Currency.EUR());

        assertThrows(NullPointerException.class, () -> new OrderItem(null, q, price));
        assertThrows(NullPointerException.class, () -> new OrderItem(pid, null, price));
        assertThrows(NullPointerException.class, () -> new OrderItem(pid, q, null));
    }

    @Test
    void totalIsUnitPriceTimesQuantity() {
        ProductId pid = new ProductId("p2");
        Quantity q = new Quantity(3);
        Money price = new Money(BigDecimal.valueOf(2.5), Currency.EUR());
        OrderItem item = new OrderItem(pid, q, price);

        Money total = item.total();
        assertEquals(new java.math.BigDecimal("7.50"), total.getAmount());
        assertEquals(Currency.EUR(), total.getCurrency());
    }

    @Test
    void increaseQuantityCreatesNewItemWithAddedQuantity() {
        ProductId pid = new ProductId("p3");
        OrderItem base = new OrderItem(pid, new Quantity(2), new Money(BigDecimal.valueOf(1), Currency.EUR()));
        OrderItem increased = base.increaseQuantity(new Quantity(4));

        assertEquals(6, increased.getQuantity().getValue());
        // unit price unchanged
        assertEquals(base.getUnitPrice(), increased.getUnitPrice());
    }
}
