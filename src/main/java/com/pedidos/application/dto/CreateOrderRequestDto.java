package com.pedidos.application.dto;

import java.util.Collections;
import java.util.List;

/**
 * DTO para crear un pedido: lista de líneas con productId, quantity, unitPrice
 * y currency code.
 */
public final class CreateOrderRequestDto {
    private final List<CreateOrderItemDto> items;

    public CreateOrderRequestDto(List<CreateOrderItemDto> items) {
        this.items = items == null ? Collections.emptyList() : Collections.unmodifiableList(items);
    }

    public List<CreateOrderItemDto> getItems() {
        return items;
    }
}
