package com.pedidos.application.dto;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * DTO para añadir un item a un pedido existente.
 */
public final class AddItemToOrderRequest {
    public final String orderId; // UUID string
    public final String productId;
    public final int quantity;
    public final BigDecimal unitPrice;
    public final String currency;

    public AddItemToOrderRequest(String orderId, String productId, int quantity, BigDecimal unitPrice,
            String currency) {
        this.orderId = Objects.requireNonNull(orderId);
        this.productId = Objects.requireNonNull(productId);
        this.quantity = quantity;
        this.unitPrice = Objects.requireNonNull(unitPrice);
        this.currency = Objects.requireNonNull(currency);
    }
}
