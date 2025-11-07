package com.pedidos.domain.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.pedidos.domain.errors.InvalidQuantityException;

class QuantityTest {

    @Test
    void createValidQuantity() {
        Quantity q = new Quantity(3);
        assertEquals(3, q.getValue());
    }

    @Test
    void createZeroOrNegativeThrows() {
        assertThrows(InvalidQuantityException.class, () -> new Quantity(0));
        assertThrows(InvalidQuantityException.class, () -> new Quantity(-5));
    }

    @Test
    void addQuantitiesProducesSum() {
        Quantity a = new Quantity(2);
        Quantity b = new Quantity(5);
        Quantity sum = a.add(b);
        assertEquals(7, sum.getValue());
    }

    @Test
    void equalsAndHashCode() {
        Quantity a = new Quantity(4);
        Quantity b = new Quantity(4);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
