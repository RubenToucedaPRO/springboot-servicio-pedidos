package com.pedidos.application.dto;

import java.math.BigDecimal;

/**
 * DTO reutilizable para representar una línea de pedido (transport DTO).
 * Usado por CreateOrderRequestDto y por el endpoint de añadir item.
 * Campos públicos para facilitar la deserialización por Jackson.
 */
public final class CreateOrderItemDto {
    public String productId;
    public Integer quantity;
    public BigDecimal unitPrice;
    public String currency;

    public CreateOrderItemDto() {
    }

    public CreateOrderItemDto(String productId, Integer quantity, BigDecimal unitPrice, String currency) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.currency = currency;
    }
}