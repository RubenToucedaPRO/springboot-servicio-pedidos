package com.pedidos.application.dto;

import java.math.BigDecimal;

/**
 * DTO para añadir un item a un pedido existente.
 */
public final class AddItemToOrderRequestDto {
    public final String orderId; // UUID string
    public final String productId;
    public final int quantity;
    public final BigDecimal unitPrice;
    public final String currency;

    public AddItemToOrderRequestDto(String orderId, String productId, int quantity, BigDecimal unitPrice,
            String currency) {
        // No validar con requireNonNull aquí: dejar que el caso de uso haga la
        // validación
        // para poder devolver errores controlados en vez de NullPointerException.
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.currency = currency;
    }
}
