package com.pedidos.application.dto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * DTO para crear un pedido: lista de líneas con productId, quantity, unitPrice
 * y currency code.
 */
public final class CreateOrderRequest {
    public static final class Item {
        public final String productId;
        public final int quantity;
        public final BigDecimal unitPrice;
        public final String currency; // e.g. "EUR"

        public Item(String productId, int quantity, BigDecimal unitPrice, String currency) {
            this.productId = Objects.requireNonNull(productId);
            this.quantity = quantity;
            this.unitPrice = Objects.requireNonNull(unitPrice);
            this.currency = Objects.requireNonNull(currency);
        }
    }

    private final List<Item> items;

    public CreateOrderRequest(List<Item> items) {
        this.items = items == null ? Collections.emptyList() : Collections.unmodifiableList(items);
    }

    public List<Item> getItems() {
        return items;
    }
}
