package com.pedidos.application.dto;

import java.util.Collections;
import java.util.List;

/**
 * DTO para crear un pedido: lista de líneas con productId, quantity, unitPrice
 * y currency code.
 */
public final class OrderDto {
    private final List<ItemDto> items;

    public OrderDto(List<ItemDto> items) {
        this.items = items == null ? Collections.emptyList() : Collections.unmodifiableList(items);
    }

    public List<ItemDto> getItems() {
        return items;
    }
}
