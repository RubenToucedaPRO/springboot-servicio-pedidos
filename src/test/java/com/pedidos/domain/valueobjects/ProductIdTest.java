package com.pedidos.domain.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.pedidos.domain.errors.InvalidProductIdException;

class ProductIdTest {

    @Test
    void nullIdThrows() {
        assertThrows(InvalidProductIdException.class, () -> new ProductId(null));
    }

    @Test
    void emptyOrBlankThrows() {
        assertThrows(InvalidProductIdException.class, () -> new ProductId(""));
        assertThrows(InvalidProductIdException.class, () -> new ProductId("   "));
    }

    @Test
    void trimmedAndEquals() {
        ProductId a = new ProductId("  SKU-123 ");
        ProductId b = new ProductId("SKU-123");
        assertEquals("SKU-123", a.getId());
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
