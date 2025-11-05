package com.pedidos.application.dto;

/**
 * DTO para añadir un item a un pedido existente. Ahora compuesto: contiene el
 * `orderId` y un `ItemDto` reutilizable que describe la línea (productId,
 * quantity, unitPrice, currency).
 */
public final class ItemToOrderDto {
    public final String orderId; // UUID string
    public final ItemDto item;

    public ItemToOrderDto(String orderId, ItemDto item) {
        // No validar con requireNonNull aquí: dejar que el caso de uso haga la
        // validación y devuelva errores controlados.
        this.orderId = orderId;
        this.item = item;
    }
}
