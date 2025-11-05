package com.pedidos.application.dto;

import java.math.BigDecimal;

/**
 * DTO reutilizable para representar una línea de pedido (transport DTO).
 * Usado por CreateOrderRequestDto y por el endpoint de añadir item.
 * Campos públicos para facilitar la deserialización por Jackson.
 */
public final class ItemDto {
    public String productId;
    public Integer quantity;
    public BigDecimal unitPrice;
    public String currency;

    public ItemDto() {
    }

    public ItemDto(String productId, Integer quantity, BigDecimal unitPrice, String currency) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.currency = currency;
    }
}